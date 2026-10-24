package app.futuresquare.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.futuresquare.R

object Ink {
    val Paper = Color(0xFFF6F8FC)
    val Rule = Color(0xFFD6E0F0)
    val Margin = Color(0xFFE7A3A3)
    val Blue = Color(0xFF1D3A9E)
    val BlueSoft = Color(0xFF4A5F9E)
    val Text = Color(0xFF1D2433)
    val Muted = Color(0xFF5B6478)
    val Red = Color(0xFFC8322B)
    val Hilite = Color(0xFFFFE680)
    val Card = Color(0xFFFFFFFF)
    val Line = Color(0xFFC9D3E6)
    val Desk = Color(0xFFB9844F)
    val DeskDark = Color(0xFF9C6B3C)
    val Cover = Color(0xFFEF6F4C)
}

/** The dreamy sky behind the game. */
object Sky {
    val Top = Color(0xFF5B6CF0)
    val Middle = Color(0xFF8C6CE8)
    val Bottom = Color(0xFFF49CC0)
}

val Hand = FontFamily(
    Font(R.font.caveat_semibold, FontWeight.SemiBold),
    Font(R.font.caveat_bold, FontWeight.Bold),
)

@Composable
fun FutureSquareTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ink.Blue,
            onPrimary = Ink.Paper,
            secondary = Ink.Red,
            background = Ink.Desk,
            surface = Ink.Card,
            onSurface = Ink.Text,
        ),
        content = content,
    )
}
