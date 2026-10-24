package app.futuresquare.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.TransformOrigin
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
import app.futuresquare.core.Field
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
        val place = when (state.screen) {
            Screen.CREATOR -> 0
            Screen.DESK -> 1
            else -> 2
        }
        AnimatedContent(
            targetState = place,
            transitionSpec = { (fadeIn(tween(450)) + scaleIn(initialScale = 0.94f)) togetherWith fadeOut(tween(250)) },
            label = "place",
        ) { p ->
            when (p) {
                0 -> CreatorScreen(state)
                1 -> DeskScreen(state)
                else -> PlayScreen(state)
            }
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
                StudentFigure(a, Modifier.width(150.dp).height(200.dp).bob())
            }
            OptionRow("Student") {
                Choice("👦 Boy", !a.girl) { a = a.copy(girl = false, hairStyle = 0) }
                Choice("👧 Girl", a.girl) { a = a.copy(girl = true, hairStyle = 0) }
            }
            OptionRow("Skin") { AvatarParts.skins.forEachIndexed { i, c -> Swatch(c, a.skin == i) { a = a.copy(skin = i) } } }
            OptionRow("Hair") {
                (if (a.girl) AvatarParts.girlHair else AvatarParts.boyHair).forEachIndexed { i, name ->
                    Choice(name, a.hairStyle == i) { a = a.copy(hairStyle = i) }
                }
            }
            OptionRow("Hair colour") { AvatarParts.hairColors.forEachIndexed { i, c -> Swatch(c, a.hairColor == i) { a = a.copy(hairColor = i) } } }
            OptionRow("Glasses") {
                Choice("No", !a.glasses) { a = a.copy(glasses = false) }
                Choice("👓 Yes", a.glasses) { a = a.copy(glasses = true) }
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
        Box(Modifier.popIn()) { Title("Future Square", color = Ink.Paper, size = 56) }
        Box(
            Modifier
                .bob(3f, 1800)
                .pulse(enabled = !opening, amount = 0.02f)
                .width(230.dp)
                .height(300.dp)
                .graphicsLayer {
                    rotationY = flip.value
                    cameraDistance = 16 * density
                    transformOrigin = TransformOrigin(0f, 0.5f)
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
            StudentFigure(state.player, Modifier.width(80.dp).height(110.dp).bob(4f, 1100))
            Text(
                "Tap the notebook!",
                fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Ink.Paper,
                modifier = Modifier.pulse(amount = 0.08f),
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
    LaunchedEffect(state.screen) {
        when (state.screen) {
            Screen.STRIKING, Screen.FIELDS_CHOICE -> scroll.animateScrollTo(0)
            Screen.BEGIN -> { delay(250); scroll.animateScrollTo(scroll.maxValue) }
            Screen.RESULT -> { delay(400); scroll.animateScrollTo(scroll.maxValue) }
            else -> Unit
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StudentFigure(state.subject, Modifier.width(40.dp).height(52.dp).bob(2f, 900))
                Text(
                    if (state.who == YOU) "Your future square" else "${state.who}’s future square",
                    fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Ink.Paper,
                )
            }
            NotebookPage(state.fields, state.marks, draw.value, Modifier.fillMaxWidth())
            Panel {
                if (!state.squareDrawn) {
                    Say("Drawing the future square… ✏️")
                } else {
                    AnimatedContent(
                        targetState = state.screen,
                        transitionSpec = {
                            (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(200)) { -it / 3 } + fadeOut(tween(200))) using
                                SizeTransform(clip = false)
                        },
                        label = "panel",
                    ) { screen ->
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            when (screen) {
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
                }
            }
        }
        if (state.screen == Screen.RESULT) {
            key(state.prediction) { Confetti(Modifier.fillMaxSize()) }
        }
    }
}

@Composable
private fun ColumnScope.FieldsChoicePanel(state: GameState) {
    Say(if (state.who == YOU) "How shall we play?" else "Let’s predict ${state.who}’s future!")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BigCard("📒", "Usual fields", Modifier.weight(1f), order = 0) { state.screen = Screen.VALUES }
        BigCard("✨", "Pick my own", Modifier.weight(1f), order = 1) { state.screen = Screen.PICK_FIELDS }
    }
    TextButton(onClick = { state.editStudent() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(if (state.who == YOU) "Change my student" else "Change ${state.who}’s student", color = Ink.BlueSoft)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.PickFieldsPanel(state: GameState) {
    Say("Pick your fields")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.fields.forEach { f ->
            key(f.id) {
                Box(Modifier.popIn()) {
                    Choice("${Art.kindIcon(f.kind)} ${f.name}" + if (state.fields.size > 2) "  ✕" else "", true) {
                        state.fields = FutureSquare.removeField(state.fields, f.id)
                    }
                }
            }
        }
    }
    Small("Tap to add (up to ${FutureSquare.MAX_FIELDS})")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldKind.entries
            .filter { !it.isClassic || state.fields.none { f -> f.kind == it } }
            .forEach { kind ->
                Choice("+ ${Art.kindIcon(kind)} ${kind.title}", false, enabled = FutureSquare.canAdd(state.fields, kind), dashed = true) {
                    state.fields = FutureSquare.addField(state.fields, kind)
                }
            }
    }
    PrimaryButton("Done") { state.screen = Screen.VALUES }
}

/** One field at a time, so there is only ever one small choice on screen. */
@Composable
private fun ColumnScope.ValuesPanel(state: GameState) {
    var step by remember { mutableIntStateOf(0) }
    var forward by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val fields = state.fields
    val index = step.coerceIn(0, fields.lastIndex)

    Row(
        Modifier.align(Alignment.CenterHorizontally),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        fields.forEachIndexed { i, _ ->
            val w by animateDpAsState(if (i == index) 24.dp else 8.dp, label = "dot")
            val c by animateColorAsState(if (i <= index) Ink.Blue else Ink.Line, label = "dotColor")
            Box(Modifier.width(w).height(8.dp).clip(CircleShape).background(c))
        }
    }
    AnimatedContent(
        targetState = index,
        transitionSpec = {
            val dir = if (forward) 1 else -1
            (slideInHorizontally(tween(260)) { dir * it / 2 } + fadeIn(tween(260))) togetherWith
                (slideOutHorizontally(tween(200)) { -dir * it / 2 } + fadeOut(tween(200))) using
                SizeTransform(clip = false)
        },
        label = "field",
    ) { i ->
        FieldValuesPicker(state, fields[i])
    }
    error?.let { Text(it, color = Ink.Red, fontSize = 14.sp, modifier = Modifier.popIn()) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (index > 0) {
            OutlinedButton(
                onClick = { error = null; forward = false; step = index - 1 },
                modifier = Modifier.height(52.dp),
            ) { Text("←") }
        }
        val last = index == fields.lastIndex
        Button(
            onClick = {
                val f = state.fields[index]
                if (f.values.size < FutureSquare.MIN_VALUES) {
                    error = "Pick at least ${FutureSquare.MIN_VALUES} for ${f.name}"
                } else if (!last) {
                    error = null
                    forward = true
                    step = index + 1
                } else {
                    state.lines = 0
                    state.screen = Screen.BEGIN
                }
            },
            modifier = Modifier.weight(1f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink.Blue, contentColor = Ink.Paper),
        ) { Text(if (last) "Ready!" else "Next →", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FieldValuesPicker(state: GameState, field: Field) {
    // Read the live field so chips update as they are tapped.
    val f = state.fields.firstOrNull { it.id == field.id } ?: field
    var typed by remember(f.id) { mutableStateOf("") }
    val add = {
        val v = typed.trim()
        if (v.isNotEmpty() && v !in f.values) state.updateField(f.id) { it.copy(values = it.values + v) }
        typed = ""
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(Art.kindIcon(f.kind), fontSize = 34.sp, modifier = Modifier.bob(2f, 1000))
            Text(f.name, fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Ink.Blue)
        }
        val options = (f.kind.suggestions + f.values).distinct()
        if (options.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEachIndexed { i, v ->
                    val on = v in f.values
                    Box(Modifier.popIn(i * 40)) {
                        Choice(listOfNotNull(Art.known(v), v).joinToString(" "), on, big = true) {
                            state.updateField(f.id) { it.copy(values = if (on) it.values - v else it.values + v) }
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = typed,
                onValueChange = { typed = it.take(20) },
                placeholder = { Text(if (f.kind == FieldKind.FRIEND) "Friend’s name" else "Your own") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { add() }),
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = add, modifier = Modifier.height(52.dp)) { Text("+ Add") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.BeginPanel(state: GameState) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
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
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
            voiceNote = "No mic? Just tap the button."
        }
    }

    Text(
        "Game begins!",
        fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 58.sp, color = Ink.Red,
        modifier = Modifier.align(Alignment.CenterHorizontally).popIn(),
    )
    // Tally sticks, one popping in per line, with a running count.
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlowRow(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(state.lines) { i ->
                key(i) {
                    Box(
                        Modifier
                            .popIn()
                            .width(4.dp)
                            .height(34.dp)
                            .graphicsLayer { rotationZ = if (i % 2 == 0) -4f else 3f }
                            .background(Ink.Blue, RoundedCornerShape(2.dp)),
                    )
                }
            }
            if (state.lines == 0) Spacer(Modifier.height(34.dp))
        }
        AnimatedContent(
            targetState = state.lines,
            transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) },
            label = "count",
        ) { n ->
            Text("$n", fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 48.sp, color = Ink.Blue)
        }
    }
    Button(
        onClick = { if (drawing) stop() else start() },
        modifier = Modifier.fillMaxWidth().height(64.dp).pulse(),
        colors = ButtonDefaults.buttonColors(containerColor = if (drawing) Ink.Red else Ink.Blue, contentColor = Ink.Paper),
    ) { Text(if (drawing) "STOP!" else "START", fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }
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
            Row(
                Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.size(12.dp).pulse(amount = 0.4f).clip(CircleShape).background(Ink.Red))
                Text("Listening… say “start” or “stop”", color = Ink.Muted, fontSize = 15.sp)
                TextButton(onClick = { voiceOn = false; voice.stop() }) { Text("Off", color = Ink.BlueSoft) }
            }
        }
    }
    (voiceNote ?: if (!voice.isAvailable) "Voice isn’t available on this phone." else null)?.let { Small(it) }
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
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(450)
            }
        }
        state.marks = BoardMarks(struck, winners = ring.filter { it !in struck }.toSet())
        delay(if (state.skip) 300L else 1100L)
        state.screen = Screen.RESULT
    }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("✏️", fontSize = 40.sp, modifier = Modifier.bob(4f, 220))
        Column(Modifier.weight(1f)) {
            Text("Counting to ${state.lines}…", fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Ink.Blue)
            AnimatedContent(
                targetState = last,
                transitionSpec = { (scaleIn(initialScale = 1.6f) + fadeIn()) togetherWith fadeOut() },
                label = "struck",
            ) { v ->
                Text(if (v == null) " " else "✗ $v", fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Ink.Red)
            }
        }
        AnimatedContent(
            targetState = state.marks.count,
            transitionSpec = { (scaleIn(initialScale = 0.4f) + fadeIn(tween(80))) togetherWith fadeOut(tween(80)) },
            label = "tick",
        ) { c ->
            Text(c?.toString() ?: "", fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 52.sp, color = Ink.Red)
        }
    }
    TextButton(onClick = { state.skip = true }, modifier = Modifier.align(Alignment.End)) { Text("Skip ⏩", color = Ink.BlueSoft) }
}

@Composable
private fun ColumnScope.ResultPanel(state: GameState) {
    val p = state.prediction ?: return
    val context = LocalContext.current
    val sentence = Sentence.build(state.who, state.fields, p.winners)
    Text(
        if (state.who == YOU) "🔮 Your future" else "🔮 ${state.who}’s future",
        fontSize = 14.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, color = Ink.Muted,
        modifier = Modifier.popIn(),
    )
    TypewriterText(sentence, color = Ink.Blue, fontSize = 30.sp, lineHeight = 34.sp)
    Box(Modifier.popIn(400)) { FuturePicture(state.subject, state.fields, p.winners) }
    Column(Modifier.popIn(1800), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PrimaryButton("📤 Share this future") {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$sentence\n\nPredicted on the last page of my notebook with Future Square ✏️")
            }
            context.startActivity(Intent.createChooser(send, "Share your future"))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigCard("🔁", "Play again", Modifier.weight(1f), order = 0) { state.newRound(YOU) }
            BigCard("🧑‍🤝‍🧑", "Predict a friend", Modifier.weight(1f), order = 1) { state.screen = Screen.WHO }
        }
    }
}

@Composable
private fun ColumnScope.WhoPanel(state: GameState) {
    var name by remember { mutableStateOf("") }
    val ok = name.isNotBlank() && !name.trim().equals(YOU, ignoreCase = true)
    Say("Whose future? 🔮")
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
    Text(text, fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Ink.Blue, lineHeight = 34.sp)
}

@Composable
private fun Small(text: String) {
    Text(text, fontSize = 14.sp, color = Ink.Muted)
}

/** A big square tile with an emoji: quicker to read than a sentence and a button. */
@Composable
private fun BigCard(icon: String, label: String, modifier: Modifier, order: Int, onClick: () -> Unit) {
    Column(
        modifier
            .popIn(order * 120)
            .clip(RoundedCornerShape(14.dp))
            .background(Ink.Paper)
            .border(2.dp, Ink.Blue, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(icon, fontSize = 40.sp, modifier = Modifier.bob(3f, 1200 + order * 200))
        Text(label, fontFamily = Hand, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Ink.Blue, textAlign = TextAlign.Center)
    }
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
private fun Choice(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    dashed: Boolean = false,
    big: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(50)
    val bg by animateColorAsState(if (selected) Ink.Blue else Color.Transparent, tween(180), label = "chipBg")
    val fg by animateColorAsState(
        when {
            selected -> Ink.Paper
            enabled -> Ink.Blue
            else -> Ink.Muted
        },
        tween(180),
        label = "chipFg",
    )
    val bounce by animateFloatAsState(if (selected) 1.06f else 1f, spring(dampingRatio = 0.35f), label = "chipBounce")
    Box(
        Modifier
            .graphicsLayer {
                scaleX = bounce
                scaleY = bounce
            }
            .clip(shape)
            .background(bg)
            .border(1.5.dp, if (enabled) Ink.Blue else Ink.Line, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (big) 16.dp else 14.dp, vertical = if (big) 11.dp else 8.dp),
    ) {
        Text(text, fontSize = if (big) 17.sp else 15.sp, color = fg, fontWeight = if (dashed) FontWeight.Normal else FontWeight.Medium)
    }
}

@Composable
private fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val ring by animateDpAsState(if (selected) 3.dp else 1.dp, label = "swatch")
    val bounce by animateFloatAsState(if (selected) 1.15f else 1f, spring(dampingRatio = 0.35f), label = "swatchBounce")
    Box(
        Modifier
            .graphicsLayer {
                scaleX = bounce
                scaleY = bounce
            }
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .border(ring, if (selected) Ink.Blue else Ink.Line, CircleShape)
            .clickable(onClick = onClick),
    )
}
