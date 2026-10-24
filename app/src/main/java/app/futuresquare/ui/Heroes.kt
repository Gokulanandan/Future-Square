package app.futuresquare.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp

enum class HeroStyle { CAPE, LONG_HAIR, CAT_EARS, HELMET }

/** An original hero: our own designs, so the app is free to publish. */
data class Hero(
    val id: String,
    val name: String,
    val power: String,
    val powerName: String,
    val suit: Color,
    val cape: Color?,
    val mask: Color,
    val hair: Color,
    val skin: Color,
    val style: HeroStyle,
)

object Heroes {
    const val STUDENT = "student"

    val all = listOf(
        Hero("zappy", "Zappy", "⚡", "Lightning zap", Color(0xFF2F6FE0), Color(0xFFE5533D), Color(0xFFE5533D), Color(0xFF1E1B18), Color(0xFFE0AC85), HeroStyle.CAPE),
        Hero("sparkle", "Sparkle Girl", "✨", "Star sparkles", Color(0xFFE85BA8), Color(0xFF7B4FD0), Color(0xFF7B4FD0), Color(0xFF5A3825), Color(0xFFC68B59), HeroStyle.LONG_HAIR),
        Hero("robo", "Robo Rex", "🔆", "Laser beam", Color(0xFF3CB4C4), null, Color(0xFF1B2A3A), Color(0xFF1B2A3A), Color(0xFFB8C4CC), HeroStyle.HELMET),
        Hero("shadow", "Shadow Cat", "🌀", "Shadow swirl", Color(0xFF2B2B3A), null, Color(0xFF2B2B3A), Color(0xFF2B2B3A), Color(0xFFF6D3B3), HeroStyle.CAT_EARS),
        Hero("comet", "Captain Comet", "☄️", "Comet crash", Color(0xFF2E9E6A), Color(0xFFF2A93B), Color(0xFFF2A93B), Color(0xFF8C3B1F), Color(0xFF8D5A3B), HeroStyle.CAPE),
    )

    fun byId(id: String?): Hero? = all.firstOrNull { it.id == id }

    /** A stable hero for a friend nobody picked one for, so the same name always gets the same hero. */
    fun forName(name: String): Hero = all[(name.trim().lowercase().hashCode() and 0x7fffffff) % all.size]
}

/** Who plays a round: one of the heroes, or the player's own customised student. */
data class Look(val heroId: String, val student: Avatar) {
    val hero: Hero? get() = Heroes.byId(heroId)
    val power: String get() = hero?.power ?: "✏️"
    val name: String get() = hero?.name ?: "Student"
}

@Composable
fun CharacterFigure(look: Look, modifier: Modifier = Modifier) {
    val hero = look.hero
    if (hero == null) StudentFigure(look.student, modifier) else HeroFigure(hero, modifier)
}

@Composable
fun HeroFigure(hero: Hero, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        Canvas(Modifier.fillMaxSize()) { drawHero(hero) }
        // The power emblem on the chest.
        val emblem = minOf(maxWidth, maxHeight) * 0.2f
        Text(
            hero.power,
            fontSize = (emblem.value * 0.8f).sp,
            modifier = Modifier.offset(x = maxWidth / 2 - emblem / 2, y = maxHeight * 0.66f),
        )
    }
}

private fun DrawScope.drawHero(h: Hero) {
    val w = size.width
    val ht = size.height
    val cx = w / 2f
    val headR = minOf(w, ht) * 0.22f
    val head = Offset(cx, ht * 0.32f)
    val dark = Color(0xFF222222)

    // Cape flowing behind the shoulders.
    h.cape?.let { cape ->
        val p = Path().apply {
            moveTo(cx - w * 0.22f, ht * 0.56f)
            lineTo(cx + w * 0.22f, ht * 0.56f)
            lineTo(cx + w * 0.4f, ht)
            lineTo(cx - w * 0.4f, ht)
            close()
        }
        drawPath(p, cape)
    }
    if (h.style == HeroStyle.LONG_HAIR) {
        drawRoundRect(h.hair, Offset(head.x - headR * 1.15f, head.y - headR * 0.5f), Size(headR * 2.3f, headR * 2.1f), CornerRadius(headR * 0.5f))
    }

    // Suit, belt and neck.
    drawRoundRect(h.suit, Offset(cx - w * 0.27f, ht * 0.56f), Size(w * 0.54f, ht * 0.44f), CornerRadius(w * 0.15f))
    drawRect(Color(0xFFFFC83D), Offset(cx - w * 0.27f, ht * 0.86f), Size(w * 0.54f, ht * 0.04f))
    drawRect(h.skin, Offset(cx - headR * 0.28f, head.y + headR * 0.8f), Size(headR * 0.56f, ht * 0.56f - head.y - headR * 0.75f))

    if (h.style == HeroStyle.HELMET) {
        // A robot helmet with a glowing visor and an antenna.
        drawRoundRect(h.suit, Offset(head.x - headR * 1.05f, head.y - headR * 1.05f), Size(headR * 2.1f, headR * 2.0f), CornerRadius(headR * 0.6f))
        drawRoundRect(h.mask, Offset(head.x - headR * 0.8f, head.y - headR * 0.35f), Size(headR * 1.6f, headR * 0.6f), CornerRadius(headR * 0.3f))
        drawRoundRect(Color(0xFF7CF5FF), Offset(head.x - headR * 0.62f, head.y - headR * 0.18f), Size(headR * 1.24f, headR * 0.22f), CornerRadius(headR * 0.11f))
        drawLine(h.mask, Offset(head.x, head.y - headR * 1.05f), Offset(head.x, head.y - headR * 1.45f), headR * 0.1f, StrokeCap.Round)
        drawCircle(Color(0xFFE5533D), headR * 0.14f, Offset(head.x, head.y - headR * 1.5f))
        drawArc(dark, 20f, 140f, false, Offset(head.x - headR * 0.3f, head.y + headR * 0.35f), Size(headR * 0.6f, headR * 0.35f), style = Stroke(headR * 0.08f, cap = StrokeCap.Round))
        return
    }

    if (h.style == HeroStyle.CAT_EARS) {
        for (side in listOf(-1f, 1f)) {
            val ear = Path().apply {
                moveTo(head.x + side * headR * 0.95f, head.y - headR * 0.35f)
                lineTo(head.x + side * headR * 0.85f, head.y - headR * 1.45f)
                lineTo(head.x + side * headR * 0.2f, head.y - headR * 0.9f)
                close()
            }
            drawPath(ear, h.suit)
        }
    }

    // Face.
    drawCircle(h.skin, headR, head)
    val capColor = if (h.style == HeroStyle.CAT_EARS) h.suit else h.hair
    drawArc(capColor, 180f, 180f, true, Offset(head.x - headR * 1.05f, head.y - headR * 1.12f), Size(headR * 2.1f, headR * 1.5f))
    // Hero mask across the eyes, with eye holes.
    drawRoundRect(h.mask, Offset(head.x - headR * 0.95f, head.y - headR * 0.18f), Size(headR * 1.9f, headR * 0.55f), CornerRadius(headR * 0.27f))
    for (side in listOf(-1f, 1f)) {
        val eye = Offset(head.x + side * headR * 0.4f, head.y + headR * 0.09f)
        drawOval(Color.White, Offset(eye.x - headR * 0.2f, eye.y - headR * 0.13f), Size(headR * 0.4f, headR * 0.26f))
        drawCircle(dark, headR * 0.08f, eye)
    }
    drawArc(Color(0xFF7A2E2E), 20f, 140f, false, Offset(head.x - headR * 0.32f, head.y + headR * 0.3f), Size(headR * 0.64f, headR * 0.45f), style = Stroke(headR * 0.09f, cap = StrokeCap.Round))
}
