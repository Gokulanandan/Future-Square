package app.futuresquare.ui

import android.content.Context
import androidx.compose.foundation.Canvas
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

/** The player's student. Every part is an index into the palettes below. */
data class Avatar(
    val girl: Boolean = false,
    val skin: Int = 1,
    val hairStyle: Int = 0,
    val hairColor: Int = 0,
    val glasses: Boolean = false,
    val uniform: Int = 0,
    val bag: Int = 0,
)

object AvatarParts {
    val skins = listOf(Color(0xFFF6D3B3), Color(0xFFE0AC85), Color(0xFFC68B59), Color(0xFF8D5A3B), Color(0xFF5C3A24))
    val hairColors = listOf(Color(0xFF1E1B18), Color(0xFF5A3825), Color(0xFF8C3B1F), Color(0xFFD9B45A), Color(0xFF3A5BD9))
    val uniforms = listOf(Color(0xFF2F5DA8), Color(0xFF2E8B57), Color(0xFFB03A48), Color(0xFF6B4FA0), Color(0xFFE0A100))
    val bags = listOf(Color(0xFFE5533D), Color(0xFF2BB3A3), Color(0xFFF2A93B), Color(0xFF8E5CD9), Color(0xFF3D3D3D))
    val boyHair = listOf("Short", "Spiky", "Curly")
    val girlHair = listOf("Plaits", "Ponytail", "Bob")
}

/** Saves avatars on the phone only: the player's own, and one per friend whose future they predicted. */
class AvatarStore(context: Context) {
    private val prefs = context.getSharedPreferences("avatars", Context.MODE_PRIVATE)

    fun soundOn(): Boolean = prefs.getBoolean("sound", true)
    fun setSoundOn(on: Boolean) = prefs.edit().putBoolean("sound", on).apply()

    fun hasPlayer(): Boolean = prefs.contains(key("me") + ".skin")
    fun player(): Avatar = load("me") ?: Avatar()
    fun savePlayer(a: Avatar) = save("me", a)
    fun friend(name: String): Avatar? = load("friend." + name.trim().lowercase())
    fun saveFriend(name: String, a: Avatar) = save("friend." + name.trim().lowercase(), a)

    private fun key(id: String) = "avatar.$id"

    private fun load(id: String): Avatar? {
        val k = key(id)
        if (!prefs.contains("$k.skin")) return null
        return Avatar(
            girl = prefs.getBoolean("$k.girl", false),
            skin = prefs.getInt("$k.skin", 1),
            hairStyle = prefs.getInt("$k.hairStyle", 0),
            hairColor = prefs.getInt("$k.hairColor", 0),
            glasses = prefs.getBoolean("$k.glasses", false),
            uniform = prefs.getInt("$k.uniform", 0),
            bag = prefs.getInt("$k.bag", 0),
        )
    }

    private fun save(id: String, a: Avatar) {
        val k = key(id)
        prefs.edit()
            .putBoolean("$k.girl", a.girl)
            .putInt("$k.skin", a.skin)
            .putInt("$k.hairStyle", a.hairStyle)
            .putInt("$k.hairColor", a.hairColor)
            .putBoolean("$k.glasses", a.glasses)
            .putInt("$k.uniform", a.uniform)
            .putInt("$k.bag", a.bag)
            .apply()
    }
}

@Composable
fun StudentFigure(avatar: Avatar, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawStudent(avatar) }
}

private fun DrawScope.drawStudent(a: Avatar) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val headR = minOf(w, h) * 0.22f
    val head = Offset(cx, h * 0.32f)
    val skin = AvatarParts.skins[a.skin.coerceIn(AvatarParts.skins.indices)]
    val hair = AvatarParts.hairColors[a.hairColor.coerceIn(AvatarParts.hairColors.indices)]
    val uniform = AvatarParts.uniforms[a.uniform.coerceIn(AvatarParts.uniforms.indices)]
    val bag = AvatarParts.bags[a.bag.coerceIn(AvatarParts.bags.indices)]
    val dark = Color(0xFF222222)

    // School bag behind the shoulder.
    drawRoundRect(bag, Offset(cx + w * 0.16f, h * 0.60f), Size(w * 0.22f, h * 0.27f), CornerRadius(w * 0.05f))

    // Hair that hangs behind the head.
    if (a.girl) {
        when (a.hairStyle) {
            0 -> { // plaits
                for (side in listOf(-1f, 1f)) {
                    val x = head.x + side * headR * 0.95f
                    drawRoundRect(hair, Offset(x - headR * 0.2f, head.y), Size(headR * 0.4f, headR * 1.9f), CornerRadius(headR * 0.2f))
                    drawCircle(Color(0xFFE5533D), headR * 0.14f, Offset(x, head.y + headR * 1.9f))
                }
            }
            1 -> drawOval(hair, Offset(head.x + headR * 0.55f, head.y - headR * 0.2f), Size(headR * 0.8f, headR * 1.7f)) // ponytail
            else -> drawRoundRect(hair, Offset(head.x - headR * 1.15f, head.y - headR * 0.4f), Size(headR * 2.3f, headR * 1.45f), CornerRadius(headR * 0.4f)) // bob
        }
    }

    // Body in school uniform, with a white collar.
    drawRoundRect(uniform, Offset(cx - w * 0.28f, h * 0.56f), Size(w * 0.56f, h * 0.44f), CornerRadius(w * 0.15f))
    drawRect(skin, Offset(cx - headR * 0.28f, head.y + headR * 0.8f), Size(headR * 0.56f, h * 0.56f - head.y - headR * 0.75f))
    val collar = Path().apply {
        moveTo(cx - w * 0.11f, h * 0.56f); lineTo(cx, h * 0.66f); lineTo(cx + w * 0.11f, h * 0.56f); close()
    }
    drawPath(collar, Color.White)
    // Bag strap across the chest.
    drawLine(bag, Offset(cx - w * 0.2f, h * 0.58f), Offset(cx + w * 0.22f, h * 0.82f), strokeWidth = w * 0.035f, cap = StrokeCap.Round)

    // Head and face.
    drawCircle(skin, headR, head)
    val eyeL = Offset(head.x - headR * 0.38f, head.y + headR * 0.1f)
    val eyeR = Offset(head.x + headR * 0.38f, head.y + headR * 0.1f)
    drawCircle(dark, headR * 0.1f, eyeL)
    drawCircle(dark, headR * 0.1f, eyeR)
    drawCircle(Color(0x55E5533D), headR * 0.16f, Offset(head.x - headR * 0.6f, head.y + headR * 0.42f))
    drawCircle(Color(0x55E5533D), headR * 0.16f, Offset(head.x + headR * 0.6f, head.y + headR * 0.42f))
    drawArc(
        Color(0xFF7A2E2E), 20f, 140f, false,
        Offset(head.x - headR * 0.33f, head.y + headR * 0.18f), Size(headR * 0.66f, headR * 0.5f),
        style = Stroke(width = headR * 0.09f, cap = StrokeCap.Round),
    )
    if (a.glasses) {
        val s = Stroke(width = headR * 0.07f)
        drawCircle(dark, headR * 0.25f, eyeL, style = s)
        drawCircle(dark, headR * 0.25f, eyeR, style = s)
        drawLine(dark, Offset(eyeL.x + headR * 0.25f, eyeL.y), Offset(eyeR.x - headR * 0.25f, eyeR.y), strokeWidth = headR * 0.07f)
    }

    // Hair on top of the head.
    val cap = Offset(head.x - headR * 1.05f, head.y - headR * 1.12f)
    val capSize = Size(headR * 2.1f, headR * 1.5f)
    drawArc(hair, 180f, 180f, true, cap, capSize)
    if (!a.girl) {
        when (a.hairStyle) {
            1 -> repeat(5) { i -> // spiky
                val x = head.x - headR * 0.8f + i * headR * 0.4f
                val spike = Path().apply {
                    moveTo(x - headR * 0.22f, head.y - headR * 0.6f)
                    lineTo(x, head.y - headR * 1.35f)
                    lineTo(x + headR * 0.22f, head.y - headR * 0.6f)
                    close()
                }
                drawPath(spike, hair)
            }
            2 -> repeat(7) { i -> // curly
                val angle = Math.toRadians(180.0 + i * 30.0)
                drawCircle(hair, headR * 0.3f, Offset(head.x + (headR * 0.95f * Math.cos(angle)).toFloat(), head.y + (headR * 0.8f * Math.sin(angle)).toFloat()))
            }
            else -> Unit
        }
    } else {
        // A fringe so the girls' styles read clearly at small sizes.
        drawArc(hair, 200f, 140f, true, Offset(head.x - headR * 0.9f, head.y - headR * 0.95f), Size(headR * 1.8f, headR * 1.1f))
    }
}
