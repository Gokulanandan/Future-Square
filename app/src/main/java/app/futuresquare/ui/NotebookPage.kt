package app.futuresquare.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.futuresquare.core.Field
import app.futuresquare.core.Side
import app.futuresquare.core.Slot

/** What the pencil has done to the square so far. */
data class BoardMarks(
    val struck: Set<Slot> = emptySet(),
    val pencilAt: Slot? = null,
    val count: Int? = null,
    val winners: Set<Slot> = emptySet(),
    /** The value just struck, where the hero fires off its power. */
    val zap: Slot? = null,
)

/** The notebook's last page: the square, its X, and the fields written around it. */
@Composable
fun NotebookPage(
    fields: List<Field>,
    marks: BoardMarks,
    drawProgress: Float,
    look: Look,
    modifier: Modifier = Modifier,
) {
    // Where each value and the square sit on the page, so the hero can move between them.
    val spots = remember { Spots() }
    // Flips once after the first layout so the hero appears without waiting for another change.
    var laidOut by remember { mutableStateOf(false) }
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Ink.Paper)
            .drawBehind {
                val step = 28.dp.toPx()
                var y = step
                while (y < size.height) {
                    drawLine(Ink.Rule, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                    y += step
                }
                drawLine(Ink.Margin, Offset(18.dp.toPx(), 0f), Offset(18.dp.toPx(), size.height), 1.5.dp.toPx())
            }
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .onGloballyPositioned {
                spots.page = it
                if (!laidOut) laidOut = true
            },
    ) {
        val showText = drawProgress >= 1f
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            fun on(side: Side) = if (showText) fields.filter { it.side == side } else emptyList()
            // Inner fields sit next to the square, outer ones further out.
            on(Side.TOP).reversed().forEach { FieldGroup(it, vertical = false, labelFirst = true, marks) { s, c -> spots.values[s] = c } }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                on(Side.LEFT).reversed().forEach { FieldGroup(it, vertical = true, labelFirst = true, marks) { s, c -> spots.values[s] = c } }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SquareDrawing(
                        drawProgress,
                        Modifier
                            .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .onGloballyPositioned { spots.square = it },
                    )
                }
                on(Side.RIGHT).forEach { FieldGroup(it, vertical = true, labelFirst = true, marks) { s, c -> spots.values[s] = c } }
            }
            on(Side.BOTTOM).forEach { FieldGroup(it, vertical = false, labelFirst = false, marks) { s, c -> spots.values[s] = c } }
        }

        // The hero stands on the pencil tip while the square is drawn, then on each value it counts.
        // In between it waits in the middle of the X.
        val density = LocalDensity.current
        val heroW = with(density) { 30.dp.toPx() }
        val heroH = with(density) { 40.dp.toPx() }
        val square = spots.rect(spots.square)?.takeIf { laidOut }
        val tip = square?.let { r -> squareTip(drawProgress)?.let { Offset(r.left + it.x * r.width, r.top + it.y * r.height) } }
        val spot = (marks.pencilAt ?: marks.zap)?.let { spots.rect(spots.values[it]) }?.let { Offset(it.center.x, it.top) }
        val rest = square?.takeIf { drawProgress >= 1f }?.let { Offset(it.center.x, it.center.y + heroH / 2) }
        val target = tip ?: spot ?: rest
        if (target != null) {
            val feet by animateOffsetAsState(
                target,
                if (tip != null) snap() else spring(dampingRatio = 0.5f, stiffness = 500f),
                label = "hero",
            )
            CharacterFigure(
                look,
                Modifier
                    .offset { IntOffset((feet.x - heroW / 2).roundToInt(), (feet.y - heroH).roundToInt()) }
                    .size(30.dp, 40.dp)
                    .bob(2f, 240),
            )
        }
        marks.zap?.let { z ->
            spots.rect(spots.values[z])?.let { r -> key(z) { PowerBurst(look.power, r.center) } }
        }
    }
}

/** Layout positions, read when the hero moves rather than kept as state, so scrolling recomposes nothing. */
private class Spots {
    var page: LayoutCoordinates? = null
    var square: LayoutCoordinates? = null
    val values = HashMap<Slot, LayoutCoordinates>()

    fun rect(c: LayoutCoordinates?): Rect? {
        val p = page ?: return null
        if (c == null || !c.isAttached || !p.isAttached) return null
        return p.localBoundingBoxOf(c, clipBounds = false)
    }
}

/** Where the pencil tip is (0..1 across the square), or null when the square isn't being drawn. */
private fun squareTip(progress: Float): Offset? {
    if (progress <= 0f || progress >= 1f) return null
    val i = 0.04f
    val tl = Offset(i, i)
    val tr = Offset(1 - i, i)
    val br = Offset(1 - i, 1 - i)
    val bl = Offset(i, 1 - i)
    val segments = listOf(tl to tr, tr to br, br to bl, bl to tl, tl to br, tr to bl)
    val k = (progress * segments.size).toInt().coerceAtMost(segments.lastIndex)
    val (a, b) = segments[k]
    return lerp(a, b, progress * segments.size - k)
}

/** The hero's power going off on the struck value: a big emoji and a ring of small ones. */
@Composable
private fun PowerBurst(power: String, center: Offset) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(650)) }
    val density = LocalDensity.current
    val half = with(density) { 18.dp.toPx() }
    val reach = with(density) { 34.dp.toPx() }
    Box(
        Modifier
            .offset { IntOffset((center.x - half).roundToInt(), (center.y - half).roundToInt()) }
            .size(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        repeat(6) { i ->
            val angle = Math.toRadians(i * 60.0 - 90.0)
            Text(
                power,
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer {
                    translationX = (Math.cos(angle) * reach * t.value).toFloat()
                    translationY = (Math.sin(angle) * reach * t.value).toFloat()
                    alpha = 1f - t.value
                },
            )
        }
        Text(
            power,
            fontSize = 26.sp,
            modifier = Modifier.graphicsLayer {
                val s = 0.4f + 1.4f * t.value
                scaleX = s
                scaleY = s
                alpha = (1.3f - t.value).coerceIn(0f, 1f)
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FieldGroup(
    field: Field,
    vertical: Boolean,
    labelFirst: Boolean,
    marks: BoardMarks,
    onPlaced: (Slot, LayoutCoordinates) -> Unit,
) {
    val left = field.values.indices.count { Slot(field.id, it) !in marks.struck }
    val closed = marks.struck.isNotEmpty() && left <= 1
    val label = @Composable {
        Text(
            (field.name + if (closed) " · closed" else "").uppercase(),
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (closed) Ink.Red else Ink.Muted,
        )
    }
    val values = @Composable {
        field.values.forEachIndexed { i, v ->
            val slot = Slot(field.id, i)
            ValueText(
                v,
                order = i,
                struck = slot in marks.struck,
                pencil = slot == marks.pencilAt,
                count = if (slot == marks.pencilAt) marks.count else null,
                winner = slot in marks.winners,
                onPlaced = { onPlaced(slot, it) },
            )
        }
        if (field.values.isEmpty()) Text("…", fontFamily = Hand, fontSize = 24.sp, color = Ink.Muted)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (labelFirst) label()
        if (vertical) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { values() }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) { values() }
        }
        if (!labelFirst) label()
    }
}

@Composable
private fun ValueText(
    text: String,
    order: Int,
    struck: Boolean,
    pencil: Boolean,
    count: Int?,
    winner: Boolean,
    onPlaced: (LayoutCoordinates) -> Unit,
) {
    val strike by animateFloatAsState(if (struck) 1f else 0f, tween(320), label = "strike")
    val ring by animateFloatAsState(if (winner) 1f else 0f, tween(600), label = "winner")
    val ink by animateColorAsState(if (struck) Ink.Muted else Ink.Blue, tween(320), label = "ink")
    val glow by animateColorAsState(if (pencil) Ink.Hilite else Color.Transparent, tween(90), label = "glow")
    val lift by animateFloatAsState(if (pencil) 1.15f else 1f, spring(dampingRatio = 0.5f), label = "lift")
    Box(contentAlignment = Alignment.Center, modifier = Modifier.popIn(delayMs = order * 70)) {
        Text(
            text,
            fontFamily = Hand,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = ink,
            modifier = Modifier
                .padding(top = 6.dp)
                .onGloballyPositioned(onPlaced)
                .graphicsLayer {
                    scaleX = lift
                    scaleY = lift
                }
                .background(glow, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp)
                .drawWithContent {
                    drawContent()
                    if (strike > 0f) {
                        val a = Offset(-2f, size.height * 0.62f)
                        val b = Offset(size.width + 2f, size.height * 0.45f)
                        drawLine(Ink.Red, a, lerp(a, b, strike), 3.dp.toPx(), StrokeCap.Round)
                    }
                    if (ring > 0f) {
                        val pad = 3.dp.toPx()
                        drawArc(
                            Ink.Red, -90f, 360f * ring, false,
                            Offset(-pad, -pad), Size(size.width + 2 * pad, size.height + 2 * pad),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                },
        )
        if (pencil && count != null) {
            Text(
                "$count",
                color = Ink.Red,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart).offset(x = (-6).dp, y = (-6).dp).popIn(),
            )
        }
    }
}

/** Draws the square then the X, stroke by stroke; the hero rides the tip. */
@Composable
private fun SquareDrawing(progress: Float, modifier: Modifier) {
    Canvas(modifier) {
        val inset = size.minDimension * 0.04f
        val tl = Offset(inset, inset)
        val tr = Offset(size.width - inset, inset)
        val br = Offset(size.width - inset, size.height - inset)
        val bl = Offset(inset, size.height - inset)
        val segments = listOf(tl to tr, tr to br, br to bl, bl to tl, tl to br, tr to bl)
        segments.forEachIndexed { k, (a, b) ->
            val t = (progress * segments.size - k).coerceIn(0f, 1f)
            if (t > 0f) drawLine(Ink.Blue, a, lerp(a, b, t), 3.dp.toPx(), StrokeCap.Round)
        }
    }
}
