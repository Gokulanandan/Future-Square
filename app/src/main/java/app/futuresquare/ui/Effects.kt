package app.futuresquare.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Springs the element in from small and transparent the first time it appears. */
fun Modifier.popIn(delayMs: Int = 0): Modifier = composed {
    val scale = remember { Animatable(0.3f) }
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        launch { scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) }
        fade.animateTo(1f, tween(180))
    }
    Modifier.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        alpha = fade.value
    }
}

/** Gentle up-and-down float, for characters and things waiting to be tapped. */
fun Modifier.bob(amountDp: Float = 4f, periodMs: Int = 1400): Modifier = composed {
    val t = rememberInfiniteTransition(label = "bob")
    val y by t.animateFloat(
        initialValue = -amountDp,
        targetValue = amountDp,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bobY",
    )
    Modifier.graphicsLayer { translationY = y * density }
}

/** A slow heartbeat that says "tap me". */
fun Modifier.pulse(enabled: Boolean = true, amount: Float = 0.05f): Modifier = composed {
    if (!enabled) return@composed Modifier
    val t = rememberInfiniteTransition(label = "pulse")
    val s by t.animateFloat(
        initialValue = 1f,
        targetValue = 1f + amount,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )
    Modifier.graphicsLayer {
        scaleX = s
        scaleY = s
    }
}

private class ConfettiPiece(
    val x: Float,
    val speed: Float,
    val drift: Float,
    val sizeDp: Float,
    val color: Color,
    val spin: Float,
    val delay: Float,
)

private val confettiColors = listOf(
    Color(0xFFE5533D), Color(0xFF2BB3A3), Color(0xFFF2A93B), Color(0xFF8E5CD9), Color(0xFF2F5DA8), Color(0xFFFFD43B),
)

/** Paper confetti falling once over the page. Draws nothing after it has fallen. */
@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val pieces = remember {
        List(80) {
            ConfettiPiece(
                x = Random.nextFloat(),
                speed = 0.7f + Random.nextFloat() * 0.8f,
                drift = (Random.nextFloat() - 0.5f) * 0.3f,
                sizeDp = 6f + Random.nextFloat() * 8f,
                color = confettiColors.random(),
                spin = Random.nextFloat() * 900f,
                delay = Random.nextFloat() * 0.35f,
            )
        }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(2800, easing = LinearEasing)) }
    if (t.value >= 1f) return
    Canvas(modifier) {
        pieces.forEach { p ->
            val local = ((t.value - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val w = p.sizeDp.dp.toPx()
            val x = (p.x + p.drift * local) * size.width
            val y = -w + local * p.speed * size.height
            rotate(p.spin * local, Offset(x, y)) {
                drawRect(p.color, Offset(x - w / 2, y - w / 4), Size(w, w / 2))
            }
        }
    }
}

/** Writes the text out letter by letter, keeping the final layout from the start. */
@Composable
fun TypewriterText(text: String, color: Color, fontSize: TextUnit, lineHeight: TextUnit, msPerChar: Long = 28) {
    var shown by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        while (shown < text.length) {
            delay(msPerChar)
            shown += 1
        }
    }
    Text(
        buildAnnotatedString {
            append(text.take(shown))
            withStyle(SpanStyle(color = Color.Transparent)) { append(text.drop(shown)) }
        },
        fontFamily = Hand,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = color,
    )
}
