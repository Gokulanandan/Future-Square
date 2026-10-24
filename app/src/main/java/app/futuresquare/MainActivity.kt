package app.futuresquare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.futuresquare.ui.Ads
import app.futuresquare.ui.AvatarStore
import app.futuresquare.ui.FutureSquareApp
import app.futuresquare.ui.FutureSquareTheme
import app.futuresquare.ui.GameState

class MainActivity : ComponentActivity() {
    // The activity handles rotation and theme changes itself, so this survives them.
    private lateinit var state: GameState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light status bar icons over the dark top of the sky.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        Ads.init(this)
        state = GameState(AvatarStore(applicationContext))
        setContent {
            FutureSquareTheme { FutureSquareApp(state) }
        }
    }
}
