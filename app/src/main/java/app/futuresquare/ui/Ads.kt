package app.futuresquare.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.futuresquare.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Kid-safe ads. Every request is tagged as child-directed and under the age of consent and
 * capped at G-rated content, so AdMob only serves non-personalised, family-friendly ads.
 */
object Ads {
    // A full-screen ad only between games, after every third finished future, and never
    // twice within three minutes, so it never interrupts play.
    private const val GAMES_PER_AD = 3
    private const val MIN_GAP_MS = 3 * 60 * 1000L

    private var interstitial: InterstitialAd? = null
    private var loading = false
    private var gamesSinceAd = 0
    private var lastShownAt = 0L

    fun init(context: Context) {
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build(),
        )
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(app) {}
            launch(Dispatchers.Main) { preload(app) }
        }
    }

    private fun preload(context: Context) {
        if (interstitial != null || loading) return
        loading = true
        InterstitialAd.load(
            context,
            BuildConfig.INTERSTITIAL_AD_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                }
            },
        )
    }

    /** Call between games. Shows a full-screen ad when one is due, then carries on with [next]. */
    fun betweenGames(context: Context, next: () -> Unit) {
        gamesSinceAd++
        val activity = context.findActivity()
        val ad = interstitial
        val due = gamesSinceAd >= GAMES_PER_AD && SystemClock.elapsedRealtime() - lastShownAt >= MIN_GAP_MS
        if (activity == null || ad == null || !due) {
            next()
            if (ad == null) preload(context.applicationContext)
            return
        }
        interstitial = null
        gamesSinceAd = 0
        lastShownAt = SystemClock.elapsedRealtime()
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = preload(activity.applicationContext)
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) =
                preload(activity.applicationContext)
        }
        // Move on underneath, so closing the ad lands straight in the next game.
        next()
        ad.show(activity)
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

/** A banner that fits the screen width, shown under a finished future. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val width = LocalConfiguration.current.screenWidthDp
    val view = remember(width) {
        AdView(context).apply {
            adUnitId = BuildConfig.BANNER_AD_ID
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width))
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(view) { onDispose { view.destroy() } }
    AndroidView(factory = { view }, modifier = modifier.fillMaxWidth())
}
