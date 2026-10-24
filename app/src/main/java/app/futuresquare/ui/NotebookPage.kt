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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
)

/** The notebook's last page: the square, its X, and the fields written around it. */
@Composable
fun NotebookPage(
    fields: List<Field>,
    marks: BoardMarks,
    drawProgress: Float,
    modifier: Modifier = Modifier,
) {
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
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        val showText = drawProgress >= 1f
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            fun on(side: Side) = if (showText) fields.filter { it.side == side } else emptyList()
            // Inner fields sit next to the square, outer ones further out.
            on(Side.TOP).reversed().forEach { FieldGroup(it, vertical = false, labelFirst = true, marks) }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                on(Side.LEFT).reversed().forEach { FieldGroup(it, vertical = true, labelFirst = true, marks) }
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SquareDrawing(
                        drawProgress,
                        Modifier.sizeIn(maxWidth = 200.dp, maxHeight = 200.dp).fillMaxWidth().aspectRatio(1f),
                    )
                }
                on(Side.RIGHT).forEach { FieldGroup(it, vertical = true, labelFirst = true, marks) }
            }
            on(Side.BOTTOM).forEach { FieldGroup(it, vertical = false, labelFirst = false, marks) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FieldGroup(field: Field, vertical: Boolean, labelFirst: Boolean, marks: BoardMarks) {
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
private fun ValueText(text: String, order: Int, struck: Boolean, pencil: Boolean, count: Int?, winner: Boolean) {
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
        if (pencil) {
            Text("✏️", fontSize = 15.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-8).dp).bob(3f, 160))
            if (count != null) {
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
}

/** Draws the square then the X, stroke by stroke, with the pencil at the tip. */
@Composable
private fun SquareDrawing(progress: Float, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier) {
        val inset = size.minDimension * 0.04f
        val tl = Offset(inset, inset)
        val tr = Offset(size.width - inset, inset)
        val br = Offset(size.width - inset, size.height - inset)
        val bl = Offset(inset, size.height - inset)
        val segments = listOf(tl to tr, tr to br, br to bl, bl to tl, tl to br, tr to bl)
        var tip: Offset? = null
        segments.forEachIndexed { k, (a, b) ->
            val t = (progress * segments.size - k).coerceIn(0f, 1f)
            if (t > 0f) {
                val end = lerp(a, b, t)
                drawLine(Ink.Blue, a, end, 3.dp.toPx(), StrokeCap.Round)
                if (t < 1f) tip = end
            }
        }
        tip?.let { drawText(measurer, "✏️", topLeft = Offset(it.x, it.y - 22.dp.toPx())) }
    }
}
