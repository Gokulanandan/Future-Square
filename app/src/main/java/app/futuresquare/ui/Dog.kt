package app.futuresquare.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.futuresquare.core.Reaction
import kotlinx.coroutines.delay

/** One appearance of the dog: [id] changes every time so the same reaction can pop up twice. */
data class DogMoment(val id: Int, val reaction: Reaction, val caption: String)

/** The dog pops up from the bottom of the screen, reacts, and ducks back down. */
@Composable
fun DogPopup(moment: DogMoment?, modifier: Modifier = Modifier) {
    if (moment == null || moment.reaction == Reaction.NONE) return
    val rise = remember(moment.id) { Animatable(1f) }
    LaunchedEffect(moment.id) {
        when (moment.reaction) {
            Reaction.WOW -> Sfx.wow()
            Reaction.LAUGH -> Sfx.laugh()
            Reaction.NONE -> Unit
        }
        rise.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow))
        delay(1900)
        rise.animateTo(1.2f, tween(350))
    }
    val shake = rememberInfiniteTransition(label = "dogShake")
    val wiggle by shake.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(if (moment.reaction == Reaction.LAUGH) 110 else 420), RepeatMode.Reverse),
        label = "wiggle",
    )
    Column(
        modifier.graphicsLayer {
            translationY = rise.value * size.height
            rotationZ = wiggle * if (moment.reaction == Reaction.LAUGH) 1f else 0.4f
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            moment.caption,
            fontFamily = Hand,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = if (moment.reaction == Reaction.WOW) Ink.Blue else Ink.Red,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
        Box(Modifier.size(150.dp)) {
            Canvas(Modifier.size(150.dp)) { drawDog(moment.reaction) }
        }
    }
}

private val Fur = Color(0xFFE8B07A)
private val FurLight = Color(0xFFF7DDBE)
private val Ear = Color(0xFF8A5A33)
private val Dark = Color(0xFF2B1D14)

private fun DrawScope.drawDog(reaction: Reaction) {
    val w = size.width
    val c = Offset(w / 2, size.height * 0.56f)
    val r = w * 0.34f

    // Floppy ears behind the head.
    rotate(25f, Offset(c.x - r * 0.8f, c.y - r * 0.6f)) {
        drawOval(Ear, Offset(c.x - r * 1.15f, c.y - r * 0.85f), Size(r * 0.62f, r * 1.25f))
    }
    rotate(-25f, Offset(c.x + r * 0.8f, c.y - r * 0.6f)) {
        drawOval(Ear, Offset(c.x + r * 0.53f, c.y - r * 0.85f), Size(r * 0.62f, r * 1.25f))
    }
    // Head and muzzle.
    drawCircle(Fur, r, c)
    drawOval(FurLight, Offset(c.x - r * 0.55f, c.y + r * 0.05f), Size(r * 1.1f, r * 0.8f))
    // A patch over one eye.
    drawOval(Ear.copy(alpha = 0.5f), Offset(c.x + r * 0.12f, c.y - r * 0.62f), Size(r * 0.55f, r * 0.5f))

    val eyeL = Offset(c.x - r * 0.38f, c.y - r * 0.32f)
    val eyeR = Offset(c.x + r * 0.38f, c.y - r * 0.32f)
    val nose = Offset(c.x, c.y + r * 0.18f)

    when (reaction) {
        Reaction.LAUGH -> {
            // Squeezed-shut happy eyes, a huge open mouth and tears of laughter.
            val s = Stroke(width = r * 0.1f, cap = StrokeCap.Round)
            drawArc(Dark, 200f, 140f, false, Offset(eyeL.x - r * 0.2f, eyeL.y - r * 0.05f), Size(r * 0.4f, r * 0.3f), style = s)
            drawArc(Dark, 200f, 140f, false, Offset(eyeR.x - r * 0.2f, eyeR.y - r * 0.05f), Size(r * 0.4f, r * 0.3f), style = s)
            drawArc(Dark, 0f, 180f, true, Offset(c.x - r * 0.42f, c.y + r * 0.2f), Size(r * 0.84f, r * 0.7f))
            drawOval(Color(0xFFF07A8A), Offset(c.x - r * 0.2f, c.y + r * 0.5f), Size(r * 0.4f, r * 0.32f))
            drawCircle(Color(0xFF7CC6FF), r * 0.09f, Offset(eyeL.x - r * 0.28f, eyeL.y + r * 0.2f))
            drawCircle(Color(0xFF7CC6FF), r * 0.09f, Offset(eyeR.x + r * 0.28f, eyeR.y + r * 0.2f))
        }
        else -> {
            // Wide eyes with sparkles and a little "o" mouth.
            for (e in listOf(eyeL, eyeR)) {
                drawCircle(Color.White, r * 0.2f, e)
                drawCircle(Dark, r * 0.12f, e)
                drawCircle(Color.White, r * 0.04f, Offset(e.x + r * 0.04f, e.y - r * 0.05f))
            }
            val brow = Stroke(width = r * 0.07f, cap = StrokeCap.Round)
            drawLine(Dark, Offset(eyeL.x - r * 0.18f, eyeL.y - r * 0.34f), Offset(eyeL.x + r * 0.15f, eyeL.y - r * 0.4f), brow.width, StrokeCap.Round)
            drawLine(Dark, Offset(eyeR.x - r * 0.15f, eyeR.y - r * 0.4f), Offset(eyeR.x + r * 0.18f, eyeR.y - r * 0.34f), brow.width, StrokeCap.Round)
            drawOval(Dark, Offset(c.x - r * 0.13f, c.y + r * 0.42f), Size(r * 0.26f, r * 0.3f))
            for ((dx, dy) in listOf(-1.15f to -0.95f, 1.1f to -1.05f, 1.25f to 0.1f)) {
                drawStar(Offset(c.x + r * dx, c.y + r * dy), r * 0.16f)
            }
        }
    }
    drawOval(Dark, Offset(nose.x - r * 0.14f, nose.y - r * 0.1f), Size(r * 0.28f, r * 0.2f))
}

private fun DrawScope.drawStar(center: Offset, radius: Float) {
    val path = Path()
    for (i in 0 until 8) {
        val a = Math.toRadians(i * 45.0 - 90)
        val rr = if (i % 2 == 0) radius else radius * 0.35f
        val p = Offset(center.x + (rr * Math.cos(a)).toFloat(), center.y + (rr * Math.sin(a)).toFloat())
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    drawPath(path, Color(0xFFFFC83D))
}
