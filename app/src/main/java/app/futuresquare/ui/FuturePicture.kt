package app.futuresquare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.futuresquare.core.Field
import app.futuresquare.core.FieldKind
import app.futuresquare.core.Sentence

/** Stand-in art until the illustrated layers are drawn: one emoji per known value. */
object Art {
    private val byValue = mapOf(
        "doctor" to "🩺", "engineer" to "🛠️", "pilot" to "✈️", "teacher" to "📚", "astronaut" to "🚀",
        "cricketer" to "🏏", "actor" to "🎬", "scientist" to "🔬", "chef" to "🍳",
        "palace" to "🏰", "villa" to "🏡", "flat" to "🏢", "hut" to "🛖", "jail" to "⛓️", "treehouse" to "🌳", "igloo" to "🧊",
        "biryani" to "🍛", "dosa" to "🥞", "pizza" to "🍕", "noodles" to "🍜", "ice cream" to "🍨", "parotta" to "🫓",
        "dog" to "🐶", "cat" to "🐱", "parrot" to "🦜", "rabbit" to "🐰", "elephant" to "🐘", "fish" to "🐠",
        "cycle" to "🚲", "bike" to "🏍️", "car" to "🚗", "jeep" to "🚙", "helicopter" to "🚁", "auto" to "🛺",
        "flying" to "🦸", "invisibility" to "👻", "super speed" to "⚡", "time travel" to "⏳",
        "london" to "🎡", "ooty" to "⛰️", "japan" to "🗾", "the moon" to "🌙", "paris" to "🗼",
    )

    /** The emoji for a value the game knows, or null for values the player typed. */
    fun known(value: String): String? = byValue[value.trim().lowercase()]

    fun kindIcon(kind: FieldKind): String = when (kind) {
        FieldKind.AMBITION -> "🎯"
        FieldKind.RANK -> "🏅"
        FieldKind.HOUSE -> "🏠"
        FieldKind.FRIEND -> "🤝"
        FieldKind.FOOD -> "🍛"
        FieldKind.PET -> "🐾"
        FieldKind.VEHICLE -> "🚗"
        FieldKind.SUPERPOWER -> "✨"
        FieldKind.PLACE -> "🗺️"
        FieldKind.CUSTOM -> "✏️"
    }

    fun of(kind: FieldKind, value: String): String = byValue[value.trim().lowercase()] ?: when (kind) {
        FieldKind.AMBITION -> "⭐"
        FieldKind.RANK -> "🏅"
        FieldKind.HOUSE -> "🏠"
        FieldKind.FRIEND -> "🤝"
        FieldKind.FOOD -> "🍽️"
        FieldKind.PET -> "🐾"
        FieldKind.VEHICLE -> "🚗"
        FieldKind.SUPERPOWER -> "✨"
        FieldKind.PLACE -> "🗺️"
        FieldKind.CUSTOM -> "✏️"
    }
}

/** The picture of the future: the hero (or student), dressed for the job, at their house, with their friend. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FuturePicture(look: Look, fields: List<Field>, winners: Map<Int, String>, modifier: Modifier = Modifier) {
    fun winner(kind: FieldKind) = fields.firstOrNull { it.kind == kind }?.let { winners[it.id] }
    val house = winner(FieldKind.HOUSE)
    val ambition = winner(FieldKind.AMBITION)
    val friend = winner(FieldKind.FRIEND)
    val extras = fields.filter { it.kind !in setOf(FieldKind.HOUSE, FieldKind.AMBITION, FieldKind.FRIEND) }
        .mapNotNull { f -> winners[f.id]?.let { f to it } }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Ink.BlueSoft, RoundedCornerShape(12.dp))
            .background(Ink.Card),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(Brush.verticalGradient(0f to Color(0xFF8FD0FF), 0.72f to Color(0xFFDDF1FF), 0.72f to Color(0xFF8BCB6A), 1f to Color(0xFF6DB252))),
        ) {
            Text("☀️", fontSize = 30.sp, modifier = Modifier.align(Alignment.TopStart).padding(12.dp).bob(2f, 2000))
            if (house != null) {
                Column(Modifier.align(Alignment.BottomStart).padding(10.dp).popIn(500), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Art.of(FieldKind.HOUSE, house), fontSize = 70.sp)
                    Caption(house)
                }
            }
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp).width(120.dp).height(160.dp)) {
                CharacterFigure(look, Modifier.fillMaxSize().bob(3f, 900))
                if (ambition != null) {
                    Text(Art.of(FieldKind.AMBITION, ambition), fontSize = 34.sp, modifier = Modifier.align(Alignment.TopEnd).popIn(900))
                }
            }
            if (friend != null) {
                Text(
                    "Best friends with $friend 🤝",
                    fontFamily = Hand,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Ink.Blue,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .popIn(1200)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        if (ambition != null || extras.isNotEmpty()) {
            FlowRow(
                Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ambition?.let { Sticker(Art.of(FieldKind.AMBITION, it), it, 0) }
                extras.forEachIndexed { i, (f, v) ->
                    val label = if (f.kind == FieldKind.RANK) v.toIntOrNull()?.let { Sentence.ordinal(it) + " rank" } ?: v else v
                    Sticker(Art.of(f.kind, v), label, i + 1)
                }
            }
        }
    }
}

@Composable
private fun Sticker(art: String, label: String, order: Int) {
    Column(Modifier.width(76.dp).popIn(1400 + order * 150), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Text(art, fontSize = 32.sp) }
        Text(label, fontSize = 12.sp, color = Ink.Muted, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Caption(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Ink.Text,
        modifier = Modifier.background(Color(0xCCFFFFFF), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 1.dp),
    )
}
