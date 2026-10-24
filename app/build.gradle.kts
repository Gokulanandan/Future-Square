plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
val TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"
val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

fun env(name: String, fallback: String) = System.getenv(name)?.takeIf { it.isNotBlank() } ?: fallback

android {
    namespace = "app.futuresquare"
    // Google Play only accepts new apps and updates that target a recent Android version.
    compileSdk = 36

    defaultConfig {
        applicationId = "app.futuresquare"
        minSdk = 26
        targetSdk = 36
        // Every upload to Play needs a higher versionCode; CI passes its run number.
        versionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1
        versionName = "1.0.0"
    }

    // The upload key is never committed. CI decodes it from a GitHub secret and points
    // RELEASE_KEYSTORE at it; without it the release build is simply left unsigned.
    val keystore = System.getenv("RELEASE_KEYSTORE")?.let { file(it) }?.takeIf { it.exists() }
    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = keystore
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        // Google's sample IDs always serve test ads, so debug builds can never earn or
        // break AdMob rules. Release builds use the real IDs from GitHub secrets.
        debug {
            manifestPlaceholders["admobAppId"] = TEST_APP_ID
            buildConfigField("String", "BANNER_AD_ID", "\"$TEST_BANNER_ID\"")
            buildConfigField("String", "INTERSTITIAL_AD_ID", "\"$TEST_INTERSTITIAL_ID\"")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            manifestPlaceholders["admobAppId"] = env("ADMOB_APP_ID", TEST_APP_ID)
            buildConfigField("String", "BANNER_AD_ID", "\"${env("ADMOB_BANNER_ID", TEST_BANNER_ID)}\"")
            buildConfigField("String", "INTERSTITIAL_AD_ID", "\"${env("ADMOB_INTERSTITIAL_ID", TEST_INTERSTITIAL_ID)}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))

    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    // AdMob is on Google Play's list of ad SDKs self-certified for apps used by children.
    implementation("com.google.android.gms:play-services-ads:24.4.0")
}
