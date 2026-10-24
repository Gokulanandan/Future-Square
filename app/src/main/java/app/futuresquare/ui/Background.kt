package app.futuresquare.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp

private class Doodle(val icon: String, val x: Float, val y: Float, val sizeSp: Int, val periodMs: Int)

// School-bag doodles scattered over the sky, kept faint so the notebook stays the star.
private val doodles = listOf(
    Doodle("⭐", 0.08f, 0.06f, 26, 2600),
    Doodle("✏️", 0.82f, 0.12f, 30, 3100),
    Doodle("☁️", 0.55f, 0.03f, 40, 4200),
    Doodle("📐", 0.06f, 0.36f, 28, 3500),
    Doodle("✨", 0.9f, 0.4f, 24, 2300),
    Doodle("🎈", 0.12f, 0.62f, 30, 3900),
    Doodle("📚", 0.86f, 0.68f, 28, 3300),
    Doodle("🌙", 0.5f, 0.86f, 26, 4600),
    Doodle("💫", 0.2f, 0.9f, 24, 2800),
    Doodle("🍭", 0.78f, 0.92f, 28, 3700),
)

/** A bright, dreamy sky behind everything, with slowly floating doodles. */
@Composable
fun HappyBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Sky.Top, Sky.Middle, Sky.Bottom))),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val t = rememberInfiniteTransition(label = "doodles")
            doodles.forEach { d ->
                val drift by t.animateFloat(
                    initialValue = -1f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(d.periodMs), RepeatMode.Reverse),
                    label = "drift",
                )
                Text(
                    d.icon,
                    fontSize = d.sizeSp.sp,
                    modifier = Modifier
                        .offset(x = maxWidth * d.x, y = maxHeight * d.y)
                        .graphicsLayer {
                            translationY = drift * 10f * density
                            rotationZ = drift * 12f
                            alpha = 0.32f
                        },
                )
            }
        }
        content()
    }
}
