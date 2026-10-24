package app.futuresquare.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.futuresquare.core.FieldKind
import app.futuresquare.core.FutureSquare
import app.futuresquare.core.Sentence
import app.futuresquare.core.Slot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FutureSquareApp(state: GameState) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Ink.Desk)
            .drawBehind {
                // Wood grain on the classroom desk.
                val step = 46.dp.toPx()
                var y = step / 2
                while (y < size.height) {
                    drawLine(Ink.DeskDark, Offset(0f, y), Offset(size.width, y + 8.dp.toPx()), 2.dp.toPx())
                    y += step
                }
            }
            .safeDrawingPadding()
            .imePadding(),
    ) {
        when (state.screen) {
            Screen.CREATOR -> CreatorScreen(state)
            Screen.DESK -> DeskScreen(state)
            else -> PlayScreen(state)
        }
    }
}

// ---------- Make your student ----------

@Composable
private fun CreatorScreen(state: GameState) {
    var a by remember(state.creatingFor) { mutableStateOf(state.avatarBeingMade()) }
    val forName = state.creatingFor
    BackHandler(enabled = state.squareDrawn || forName != null) { state.creatingFor = null; state.screen = Screen.FIELDS_CHOICE }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Title(if (forName == null) "Make your student" else "Make $forName’s student", color = Ink.Paper)
        Panel {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StudentFigure(a, Modifier.width(150.dp).height(200.dp))
            }
            OptionRow("Student") {
                Choice("Boy", !a.girl) { a = a.copy(girl = false, hairStyle = 0) }
                Choice("Girl", a.girl) { a = a.copy(girl = true, hairStyle = 0) }
            }
            OptionRow("Skin") { AvatarParts.skins.forEachIndexed { i, c -> Swatch(c, a.skin == i) { a = a.copy(skin = i) } } }
            OptionRow("Hair style") {
                (if (a.girl) AvatarParts.girlHair else AvatarParts.boyHair).forEachIndexed { i, name ->
                    Choice(name, a.hairStyle == i) { a = a.copy(hairStyle = i) }
                }
            }
            OptionRow("Hair colour") { AvatarParts.hairColors.forEachIndexed { i, c -> Swatch(c, a.hairColor == i) { a = a.copy(hairColor = i) } } }
            OptionRow("Glasses") {
                Choice("No", !a.glasses) { a = a.copy(glasses = false) }
                Choice("Yes", a.glasses) { a = a.copy(glasses = true) }
            }
            OptionRow("Uniform") { AvatarParts.uniforms.forEachIndexed { i, c -> Swatch(c, a.uniform == i) { a = a.copy(uniform = i) } } }
            OptionRow("School bag") { AvatarParts.bags.forEachIndexed { i, c -> Swatch(c, a.bag == i) { a = a.copy(bag = i) } } }
            PrimaryButton("Done") { state.saveStudent(a) }
        }
    }
}

// ---------- Classroom desk ----------

@Composable
private fun DeskScreen(state: GameState) {
    val flip = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var opening by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Title("Future Square", color = Ink.Paper, size = 56)
        Box(
            Modifier
                .width(230.dp)
                .height(300.dp)
                .graphicsLayer {
                    rotationY = flip.value
                    cameraDistance = 16 * density
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                }
                .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                .background(Ink.Cover)
                .clickable(enabled = !opening) {
                    opening = true
                    scope.launch {
                        flip.animateTo(-90f, tween(700))
                        state.screen = Screen.FIELDS_CHOICE
                    }
                }
                .padding(20.dp),
        ) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NOTEBOOK", color = Ink.Paper, fontWeight = FontWeight.Bold, letterSpacing = 4.sp, fontSize = 20.sp)
                Spacer(Modifier.height(18.dp))
                Box(Modifier.background(Ink.Paper, RoundedCornerShape(4.dp)).padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text("Name: ________\nClass: ________", fontFamily = Hand, fontSize = 22.sp, color = Ink.Blue)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StudentFigure(state.player, Modifier.width(80.dp).height(110.dp))
            Text(
                "Tap the notebook to open the last page",
                fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Ink.Paper,
            )
        }
        TextButton(onClick = { state.editStudent() }) { Text("Change my student", color = Ink.Paper) }
    }
}

// ---------- The notebook page and the step panels ----------

@Composable
private fun PlayScreen(state: GameState) {
    val draw = remember { Animatable(if (state.squareDrawn) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!state.squareDrawn) {
            draw.animateTo(1f, tween(2400))
            state.squareDrawn = true
        }
    }
    BackHandler(enabled = state.screen in setOf(Screen.PICK_FIELDS, Screen.VALUES, Screen.WHO)) {
        state.screen = if (state.screen == Screen.WHO) Screen.RESULT else Screen.FIELDS_CHOICE
    }
    val scroll = rememberScrollState()
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StudentFigure(state.subject, Modifier.width(40.dp).height(52.dp))
            Text(
                if (state.who == YOU) "Your future square" else "${state.who}’s future square",
                fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Ink.Paper,
            )
        }
        NotebookPage(state.fields, state.marks, draw.value, Modifier.fillMaxWidth())
        Panel {
            if (!state.squareDrawn) {
                Say("Drawing the future square…")
            } else when (state.screen) {
                Screen.FIELDS_CHOICE -> FieldsChoicePanel(state)
                Screen.PICK_FIELDS -> PickFieldsPanel(state)
                Screen.VALUES -> ValuesPanel(state)
                Screen.BEGIN -> BeginPanel(state)
                Screen.STRIKING -> StrikingPanel(state)
                Screen.RESULT -> ResultPanel(state)
                Screen.WHO -> WhoPanel(state)
                else -> Unit
            }
        }
    }
    // Bring the panel into view when a step changes.
    LaunchedEffect(state.screen) { if (state.screen == Screen.RESULT) scroll.animateScrollTo(scroll.maxValue) }
}

@Composable
private fun ColumnScope.FieldsChoicePanel(state: GameState) {
    Say((if (state.who == YOU) "" else "Let’s predict ${state.who}’s future! ") + "Shall we play with the usual fields, or change them?")
    PrimaryButton("Use the usual fields") { state.screen = Screen.VALUES }
    SecondaryButton("Change fields") { state.screen = Screen.PICK_FIELDS }
    TextButton(onClick = { state.editStudent() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(if (state.who == YOU) "Change my student" else "Change ${state.who}’s student", color = Ink.BlueSoft)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.PickFieldsPanel(state: GameState) {
    Say("Pick the fields for your square")
    state.fields.forEach { f ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(f.name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Ink.Text)
            if (state.fields.size > 2) {
                TextButton(onClick = { state.fields = FutureSquare.removeField(state.fields, f.id) }) { Text("Remove", color = Ink.Red) }
            }
        }
    }
    Small("Add more fields (up to ${FutureSquare.MAX_FIELDS}):")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldKind.entries
            .filter { !it.isClassic || state.fields.none { f -> f.kind == it } }
            .forEach { kind ->
                Choice("+ ${kind.title}", false, enabled = FutureSquare.canAdd(state.fields, kind)) {
                    state.fields = FutureSquare.addField(state.fields, kind)
                }
            }
    }
    PrimaryButton("Done") { state.screen = Screen.VALUES }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.ValuesPanel(state: GameState) {
    var error by remember { mutableStateOf<String?>(null) }
    Say("Choose the values for each field")
    state.fields.forEach { f ->
        key(f.id) {
            var typed by remember { mutableStateOf("") }
            val add = {
                val v = typed.trim()
                if (v.isNotEmpty() && v !in f.values) state.updateField(f.id) { it.copy(values = it.values + v) }
                typed = ""
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    f.name.uppercase() + if (f.kind == FieldKind.FRIEND) " · only you know these!" else "",
                    fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, color = Ink.Muted,
                )
                val options = (f.kind.suggestions + f.values).distinct()
                if (options.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { v ->
                            val on = v in f.values
                            Choice(v, on) {
                                state.updateField(f.id) { it.copy(values = if (on) it.values - v else it.values + v) }
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it.take(20) },
                        placeholder = { Text(if (f.kind == FieldKind.FRIEND) "Type a friend’s name" else "Type your own") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { add() }),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(onClick = add) { Text("Add") }
                }
            }
        }
    }
    error?.let { Text(it, color = Ink.Red, fontSize = 14.sp) }
    PrimaryButton("Ready!") {
        val missing = FutureSquare.fieldsMissingValues(state.fields)
        if (missing.isNotEmpty()) {
            error = "Add at least ${FutureSquare.MIN_VALUES} values to: " + missing.joinToString { it.name }
        } else {
            state.lines = 0
            state.screen = Screen.BEGIN
        }
    }
}

@Composable
private fun ColumnScope.BeginPanel(state: GameState) {
    val context = LocalContext.current
    var drawing by remember { mutableStateOf(false) }
    val pending = remember { mutableStateOf<VoiceCommand?>(null) }
    val voice = remember { VoiceCommands(context.applicationContext) { pending.value = it } }
    var voiceOn by remember { mutableStateOf(false) }
    var voiceNote by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) { onDispose { voice.destroy() } }

    fun start() {
        if (drawing) return
        state.lines = 0
        drawing = true
    }

    fun stop() {
        if (!drawing) return
        drawing = false
        voice.stop()
        state.beginStrikes()
    }

    LaunchedEffect(drawing) {
        while (drawing) {
            delay(320)
            if (!drawing) break
            state.lines += 1
            if (state.lines >= 30) stop()
        }
    }
    LaunchedEffect(pending.value) {
        when (pending.value) {
            VoiceCommand.START -> start()
            VoiceCommand.STOP -> stop()
            null -> Unit
        }
        pending.value = null
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            voiceOn = true
            voice.start()
        } else {
            voiceNote = "No microphone? No problem. Use the button."
        }
    }

    Text(
        "Game begins!",
        fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 56.sp, color = Ink.Red,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Small(if (voiceOn) "Listening… say “start”, then “stop” whenever you like." else "Tap Start, then Stop whenever you like. Or use your voice.")
    Text(
        if (state.lines == 0) " " else "|".repeat(state.lines),
        fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Ink.Blue, letterSpacing = 4.sp,
        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
    )
    if (state.lines > 0) Small("${state.lines} line${if (state.lines == 1) "" else "s"}")
    PrimaryButton(if (drawing) "Stop!" else "Start") { if (drawing) stop() else start() }
    if (voice.isAvailable) {
        if (!voiceOn) {
            SecondaryButton("🎤 Use my voice") {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    voiceOn = true
                    voice.start()
                } else {
                    askMic.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        } else {
            SecondaryButton("Turn voice off") { voiceOn = false; voice.stop() }
        }
    }
    (voiceNote ?: if (!voice.isAvailable) "Voice isn’t available on this phone. Use the button." else null)?.let { Small(it) }
}

@Composable
private fun ColumnScope.StrikingPanel(state: GameState) {
    val p = state.prediction ?: return
    val haptics = LocalHapticFeedback.current
    var last by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(p) {
        val ring = p.ring
        val valueOf = { s: Slot -> state.fields.first { it.id == s.fieldId }.values[s.valueIndex] }
        var struck = emptySet<Slot>()
        for (step in p.steps) {
            if (!state.skip) {
                for ((k, idx) in step.path.withIndex()) {
                    if (state.skip) break
                    state.marks = BoardMarks(struck, pencilAt = ring[idx], count = k + 1)
                    delay(if (state.lines > 8) 110L else 220L)
                }
            }
            val hit = ring[step.struck]
            struck = struck + hit
            state.marks = BoardMarks(struck)
            last = valueOf(hit)
            if (!state.skip) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(420)
            }
        }
        state.marks = BoardMarks(struck, winners = ring.filter { it !in struck }.toSet())
        delay(if (state.skip) 200L else 700L)
        state.screen = Screen.RESULT
    }
    Say("${state.lines} line${if (state.lines == 1) "" else "s"}! The pencil counts ${state.lines} and strikes…")
    last?.let { Small("Struck “$it”") }
    SecondaryButton("Skip to the end") { state.skip = true }
}

@Composable
private fun ColumnScope.ResultPanel(state: GameState) {
    val p = state.prediction ?: return
    val context = LocalContext.current
    val sentence = Sentence.build(state.who, state.fields, p.winners)
    Small(if (state.who == YOU) "Your future" else "${state.who}’s future")
    Text(sentence, fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Ink.Blue, lineHeight = 34.sp)
    FuturePicture(state.subject, state.fields, p.winners)
    PrimaryButton("Share this future") {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$sentence\n\nPredicted on the last page of my notebook with Future Square ✏️")
        }
        context.startActivity(Intent.createChooser(send, "Share your future"))
    }
    SecondaryButton("Play again") { state.newRound(YOU) }
    SecondaryButton("Predict somebody else’s future") { state.screen = Screen.WHO }
}

@Composable
private fun ColumnScope.WhoPanel(state: GameState) {
    var name by remember { mutableStateOf("") }
    val ok = name.isNotBlank() && !name.trim().equals(YOU, ignoreCase = true)
    Say("Whose future shall we predict?")
    OutlinedTextField(
        value = name,
        onValueChange = { name = it.take(20) },
        placeholder = { Text("Their name") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (ok) state.newRound(name.trim()) }),
        modifier = Modifier.fillMaxWidth(),
    )
    PrimaryButton("Let’s go", enabled = ok) { state.newRound(name.trim()) }
    SecondaryButton("Make their student first", enabled = ok) { state.makeFriendStudent(name) }
}

// ---------- Small building blocks ----------

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ink.Card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun Title(text: String, color: Color, size: Int = 40) {
    Text(text, fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = size.sp, color = color, textAlign = TextAlign.Center)
}

@Composable
private fun Say(text: String) {
    Text(text, fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Ink.Blue, lineHeight = 32.sp)
}

@Composable
private fun Small(text: String) {
    Text(text, fontSize = 14.sp, color = Ink.Muted)
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Ink.Blue, contentColor = Ink.Paper),
    ) { Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun SecondaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(48.dp)) {
        Text(text, fontSize = 16.sp, color = if (enabled) Ink.Blue else Ink.Muted)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label.uppercase(), fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, color = Ink.Muted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun Choice(text: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) Ink.Blue else Color.Transparent)
            .border(1.5.dp, if (enabled) Ink.Blue else Ink.Line, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, fontSize = 15.sp, color = when {
            selected -> Ink.Paper
            enabled -> Ink.Blue
            else -> Ink.Muted
        })
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 3.dp else 1.dp, if (selected) Ink.Blue else Ink.Line, CircleShape)
            .clickable(onClick = onClick),
    )
}
