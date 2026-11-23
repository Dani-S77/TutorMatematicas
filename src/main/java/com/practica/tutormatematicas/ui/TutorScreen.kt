package com.practica.tutormatematicas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF0A0E15)
private val PanelBg = Color(0xFF10161F)
private val EditorBg = Color(0xFF0A0F17)
private val Border = Color(0xFF232C39)
private val TextMain = Color(0xFFE8ECF2)
private val TextSoft = Color(0xFF8993A6)
private val TextFaint = Color(0xFF5C6577)
private val Accent = Color(0xFF34D399)
private val AccentStrong = Color(0xFF22C55E)
private val AccentBg = Color(0x1A34D399)
private val AccentBorder = Color(0x5934D399)
private val ErrorColor = Color(0xFFF87171)
private val ErrorBg = Color(0x14F87171)
private val ErrorBorder = Color(0x59F87171)

@Composable
fun TutorApp(vm: TutorViewModel) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = PanelBg,
            primary = Accent,
            onPrimary = Color(0xFF06230F),
            onBackground = TextMain,
            onSurface = TextMain
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Header(vm)
            Spacer(Modifier.height(18.dp))
            LessonCard(vm)
            Spacer(Modifier.height(14.dp))
            TopicPills(vm)
            Spacer(Modifier.height(16.dp))
            ExercisePanel(vm)
            Spacer(Modifier.height(14.dp))
            ResultPanel(vm)
            Spacer(Modifier.height(16.dp))
            Actions(vm)
        }
    }
}

@Composable
private fun Header(vm: TutorViewModel) {
    val total = vm.view.totalSteps
    val done = vm.stepsDone
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .background(AccentBg, RoundedCornerShape(5.dp))
                .border(BorderStroke(1.dp, AccentBorder), RoundedCornerShape(5.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text("MATEMÁTICAS", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.size(10.dp))
        Text("Práctica guiada", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("paso $done/$total", color = TextFaint, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
    Spacer(Modifier.height(10.dp))
    val fraction = if (vm.view.topicKey == null) 0f else (done.toFloat() / total).coerceIn(0f, 1f)
    LinearProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().height(5.dp),
        color = AccentStrong,
        trackColor = Border
    )
}

@Composable
private fun LessonCard(vm: TutorViewModel) {
    val v = vm.view
    val badge = when (v.phaseName) {
        "done" -> "★"
        "idle" -> "–"
        else -> (v.stepsDone + 1).toString()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg, RoundedCornerShape(11.dp))
            .border(BorderStroke(1.dp, Border), RoundedCornerShape(11.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(AccentBg, RoundedCornerShape(10.dp))
                .border(BorderStroke(1.dp, AccentBorder), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(badge, color = Accent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(14.dp))
        Column {
            Text(v.lessonTitle, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(v.lessonSubtitle, color = TextSoft, fontSize = 12.5.sp)
        }
    }
}

@Composable
private fun TopicPills(vm: TutorViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        vm.topics.forEach { topic ->
            val active = vm.view.topicKey == topic.key
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (active) AccentBg else PanelBg, RoundedCornerShape(20.dp))
                    .border(
                        BorderStroke(1.dp, if (active) AccentBorder else Border),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { vm.selectTopic(topic.key) }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    topic.displayName,
                    color = if (active) Accent else TextSoft,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ExercisePanel(vm: TutorViewModel) {
    val v = vm.view
    val enabled = vm.isActive && !vm.waitingForNext && !vm.loading
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditorBg, RoundedCornerShape(11.dp))
            .border(BorderStroke(1.dp, Border), RoundedCornerShape(11.dp))
            .padding(16.dp)
    ) {
        Text("EJERCICIO", color = TextFaint, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        val body = when {
            v.question != null -> v.question
            v.phaseName == "done" -> "Ronda completada. Elige un tema o reinicia para seguir practicando."
            else -> "Elige un tema para empezar."
        }
        Text(
            body,
            color = if (v.question != null) TextMain else TextSoft,
            fontFamily = if (v.question != null) FontFamily.Monospace else FontFamily.Default,
            fontSize = if (v.question != null) 22.sp else 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = vm.input,
            onValueChange = vm::onInputChange,
            enabled = enabled,
            singleLine = true,
            placeholder = { Text("tu respuesta, ej: 3/4 ó 4", color = TextFaint) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { vm.submit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                disabledTextColor = TextSoft,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                disabledBorderColor = Border,
                cursorColor = Accent
            )
        )
        vm.hint?.let {
            Spacer(Modifier.height(10.dp))
            Text("Pista: $it", color = Accent, fontSize = 12.5.sp)
        }
        Spacer(Modifier.height(14.dp))
        if (vm.waitingForNext) {
            Button(
                onClick = vm::next,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentStrong, contentColor = Color(0xFF06230F))
            ) { Text("Continuar", fontWeight = FontWeight.ExtraBold) }
        } else {
            Button(
                onClick = vm::submit,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentStrong,
                    contentColor = Color(0xFF06230F),
                    disabledContainerColor = Border,
                    disabledContentColor = TextFaint
                )
            ) { Text(if (vm.loading) "Consultando…" else "Comprobar", fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
private fun ResultPanel(vm: TutorViewModel) {
    val r = vm.result
    val notice = vm.notice
    val (bg, border, labelColor, label, body) = when {
        r != null && r.correct -> Panel(AccentBg, AccentBorder, Accent, "CORRECTO", r.message)
        r != null -> Panel(ErrorBg, ErrorBorder, ErrorColor, "REVISA ESTO", r.message)
        notice != null -> Panel(ErrorBg, ErrorBorder, ErrorColor, "NO PUDE LEER TU RESPUESTA", notice)
        else -> Panel(EditorBg, Border, TextFaint, "SIN RESULTADOS AÚN", "")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(11.dp))
            .border(BorderStroke(1.dp, border), RoundedCornerShape(11.dp))
            .padding(14.dp)
    ) {
        Text(label, color = labelColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        if (body.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(body, color = TextSoft, fontSize = 13.5.sp, lineHeight = 20.sp)
        }
        if (r != null && !r.correct) {
            Spacer(Modifier.height(8.dp))
            Text(
                when (r.source) {
                    "MODELO" -> "Explicación generada por IA (Gemini)"
                    "RESPALDO" -> "Explicación fija (el servicio de IA no respondió)"
                    else -> "Explicación fija"
                },
                color = TextFaint,
                fontSize = 11.sp
            )
        }
    }
}

private data class Panel(
    val bg: Color,
    val border: Color,
    val labelColor: Color,
    val label: String,
    val body: String
)

@Composable
private fun Actions(vm: TutorViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = vm::showHint,
            enabled = vm.isActive && !vm.waitingForNext && !vm.loading,
            border = BorderStroke(1.dp, Border)
        ) { Text("Pista", color = TextSoft, fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
        OutlinedButton(
            onClick = vm::restart,
            enabled = !vm.loading,
            border = BorderStroke(1.dp, Border)
        ) { Text("Reiniciar", color = TextSoft, fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
    }
}
