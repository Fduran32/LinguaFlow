package com.linguaflow.app.ui.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.linguaflow.app.domain.model.Question
import com.linguaflow.app.ui.theme.*
import com.linguaflow.app.util.SpeechManager

@Composable
fun PracticeSessionScreen(
    skillMode: PracticeSkillMode,
    questions: List<Question>,
    ttsLocaleStr: String,
    onPlayAudio: (text: String, isSlow: Boolean) -> Unit,
    onWrongAnswer: ((questionId: String, wrongAnswer: String) -> Unit)? = null,
    onFinished: (score: Int) -> Unit,
    onClose: () -> Unit
) {
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text(text = "No hay ejercicios disponibles en este momento.", color = TextDark, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onClose) {
                    Text("Volver")
                }
            }
        }
        return
    }

    val context = LocalContext.current
    val speechManager = remember { SpeechManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    // Estados por pregunta
    var writtenInput by remember { mutableStateOf("") }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerChecked by remember { mutableStateOf(false) }
    var isCurrentCorrect by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }
    var hintLevel by remember(currentIndex) { mutableStateOf(0) }

    // Estados de voz
    var isListening by remember { mutableStateOf(false) }
    var soundLevel by remember { mutableStateOf(0f) }
    var recognizedSpokenText by remember { mutableStateOf("") }
    var speechSimilarityPercentage by remember { mutableStateOf(0) }
    var speechErrorMessage by remember { mutableStateOf<String?>(null) }

    // Permiso de micrófono
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
    }

    val currentQuestion = questions.getOrNull(currentIndex)

    // Animación de pulso para el botón del micrófono
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (isFinished) {
        val total = questions.size
        val scorePercent = if (total > 0) (correctCount * 100) / total else 0
        PracticeResultView(
            skillMode = skillMode,
            correctCount = correctCount,
            totalCount = total,
            scorePercent = scorePercent,
            onFinish = { onFinished(scorePercent) }
        )
        return
    }

    if (currentQuestion == null) return

    val progress = (currentIndex + 1).toFloat() / questions.size.toFloat()

    // Determinamos si el ejercicio actual es de tipo Habla, Escucha o Escritura
    val isSpeakingType = currentQuestion.type == com.linguaflow.app.domain.model.QuestionType.SPEAKING || skillMode == PracticeSkillMode.SPEAKING
    val isListeningType = !isSpeakingType && (currentQuestion.type == com.linguaflow.app.domain.model.QuestionType.LISTENING || skillMode == PracticeSkillMode.LISTENING)

    // Opción C: Generador de pistas progresivas para Writing y Listening
    val targetAnswerForHint = if (isListeningType) {
        (currentQuestion.promptAudioText ?: currentQuestion.correctAnswer).ifBlank { currentQuestion.prompt }
    } else {
        currentQuestion.correctAnswer.ifBlank { currentQuestion.prompt }
    }
    val targetWords = remember(currentQuestion.id, currentIndex) {
        targetAnswerForHint.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }
    val hintMessage = when (hintLevel) {
        1 -> "💡 Pista (1ª palabra): \"${targetWords.firstOrNull() ?: ""} ...\""
        2 -> {
            val preview = targetWords.take(2).joinToString(" ")
            "💡 Pista: \"$preview ...\" (${targetWords.size} palabras en total)"
        }
        else -> null
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IndigoBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLight)
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = PrimaryCoral,
                        trackColor = IndigoSurfaceLight
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${currentIndex + 1}/${questions.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }
        },
        containerColor = IndigoBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la Habilidad
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (skillMode) {
                    PracticeSkillMode.SPEAKING -> Color(0xFF10B981).copy(alpha = 0.15f)
                    PracticeSkillMode.WRITING -> Color(0xFF6366F1).copy(alpha = 0.15f)
                    PracticeSkillMode.LISTENING -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    PracticeSkillMode.MIX -> Color(0xFFEC4899).copy(alpha = 0.15f)
                }
            ) {
                Text(
                    text = when {
                        isSpeakingType -> "🗣️ Práctica de Pronunciación"
                        isListeningType -> "🎧 Comprensión Auditiva"
                        else -> "✍️ Práctica de Escritura"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // TIPO 1: PRÁCTICA DE SPEAKING (VOZ / MICRÓFONO IN-APP)
            // ========================================================
            if (isSpeakingType) {
                val targetText: String = currentQuestion.correctAnswer.ifBlank { currentQuestion.promptAudioText ?: currentQuestion.prompt }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Pronuncia la siguiente frase:",
                            fontSize = 14.sp,
                            color = TextMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = targetText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreenDark,
                            textAlign = TextAlign.Center
                        )

                        if (!currentQuestion.promptPhonetic.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pinyin: ${currentQuestion.promptPhonetic}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryCoral
                            )
                        }

                        if (currentQuestion.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Significado: ${currentQuestion.explanation}",
                                fontSize = 13.sp,
                                color = TextMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones de Audio de Referencia (Normal y Lento)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onPlayAudio(targetText, false) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Escuchar", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { onPlayAudio(targetText, true) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "🐢", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lento", fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // BOTÓN DE MICRÓFONO (ESTILO INTEGRADO EN LA APP)
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.2f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isListening) ErrorRed else PrimaryGreen)
                            .clickable {
                                if (!hasRecordPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    return@clickable
                                }

                                if (isListening) {
                                    speechManager.stopListening()
                                    isListening = false
                                } else {
                                    recognizedSpokenText = ""
                                    speechErrorMessage = null
                                    isAnswerChecked = false

                                    speechManager.startListening(
                                        localeStr = ttsLocaleStr,
                                        onReady = { isListening = true },
                                        onRmsChanged = { soundLevel = it },
                                        onPartialResult = { recognizedSpokenText = it },
                                        onFinalResult = { finalResult, allCandidates ->
                                            isListening = false
                                            val evaluation = SpeechManager.calculateBestSimilarity(allCandidates.ifEmpty { listOf(finalResult) }, targetText)
                                            recognizedSpokenText = evaluation.bestSpoken.ifBlank { finalResult }
                                            speechSimilarityPercentage = evaluation.percentage
                                            isCurrentCorrect = evaluation.isPassed
                                            feedbackText = evaluation.feedback
                                            isAnswerChecked = true
                                            if (!evaluation.isPassed) {
                                                onWrongAnswer?.invoke(currentQuestion.id, recognizedSpokenText)
                                            }
                                        },
                                        onError = { errorMsg ->
                                            isListening = false
                                            speechErrorMessage = errorMsg
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Micrófono",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isListening) "🎙️ Escuchando... habla ahora" else "Toca el micrófono y pronuncia la frase",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isListening) PrimaryGreenDark else TextMedium
                )

                if (recognizedSpokenText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IndigoCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Tu voz detectada:", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"$recognizedSpokenText\"",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                        }
                    }
                }

                if (speechErrorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = speechErrorMessage ?: "",
                        color = ErrorRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

            } else if (isListeningType) {
                // ========================================================
                // TIPO 2: PRÁCTICA DE LISTENING (COMPRENSIÓN AUDITIVA)
                // ========================================================
                val spokenPhrase: String = (currentQuestion.promptAudioText ?: currentQuestion.correctAnswer).ifBlank { currentQuestion.prompt }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Escucha atentamente el audio:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { onPlayAudio(spokenPhrase, false) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = IndigoBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reproducir", color = IndigoBackground, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onPlayAudio(spokenPhrase, true) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(text = "🐢", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Audio Lento", fontWeight = FontWeight.SemiBold, color = TextLight)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¿Qué frase escuchaste?",
                                fontSize = 14.sp,
                                color = TextMuted
                            )

                            if (!isAnswerChecked && hintLevel < 2) {
                                TextButton(onClick = { hintLevel++ }) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = "Pista", tint = AccentYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (hintLevel == 0) "Ver pista" else "Más pista", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                                }
                            }
                        }

                        if (hintMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentYellow.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = hintMessage,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = writtenInput,
                            onValueChange = { writtenInput = it },
                            placeholder = { Text("Escribe aquí lo que escuchas...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

            } else {
                // ========================================================
                // TIPO 3: PRÁCTICA DE WRITING (ESCRITURA DIRECTA)
                // ========================================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Traduce la siguiente frase:",
                                fontSize = 14.sp,
                                color = TextMuted
                            )

                            if (!isAnswerChecked && hintLevel < 2) {
                                TextButton(onClick = { hintLevel++ }) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = "Pista", tint = AccentYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (hintLevel == 0) "Ver pista" else "Más pista", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentQuestion.prompt,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCoral
                        )

                        if (hintMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentYellow.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = hintMessage,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = writtenInput,
                            onValueChange = { writtenInput = it },
                            placeholder = { Text("Escribe aquí tu traducción...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========================================================
            // ZONA DE FEEDBACK Y BOTÓN DE CONTINUAR
            // ========================================================
            if (isAnswerChecked) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCurrentCorrect) SuccessGreenLight else ErrorRedLight,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCurrentCorrect) SuccessGreen.copy(alpha = 0.5f) else ErrorRed.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCurrentCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCurrentCorrect) SuccessGreen else ErrorRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCurrentCorrect) {
                                    if (isSpeakingType) "¡Excelente pronunciación!" else "¡Respuesta correcta!"
                                } else if (isSpeakingType) {
                                    "Pronunciación incompleta o a mejorar:"
                                } else {
                                    "Respuesta correcta:"
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentCorrect) SuccessGreen else ErrorRed,
                                fontSize = 16.sp
                            )
                        }

                        if (!isCurrentCorrect) {
                            Text(
                                text = if (isSpeakingType) {
                                    "Frase esperada: ${currentQuestion.correctAnswer}"
                                } else if (isListeningType) {
                                    "Frase esperada: ${(currentQuestion.promptAudioText ?: currentQuestion.correctAnswer).ifBlank { currentQuestion.prompt }}"
                                } else {
                                    "Traducción esperada: ${currentQuestion.correctAnswer.ifBlank { currentQuestion.prompt }}"
                                },
                                fontSize = 15.sp,
                                color = TextLight
                            )
                        }

                        if (feedbackText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = feedbackText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrentCorrect) TextLight else ErrorRed
                            )
                        }

                        if (speechSimilarityPercentage > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isCurrentCorrect) "Similitud fonética: $speechSimilarityPercentage%" else "Similitud fonética: $speechSimilarityPercentage% (Requiere 75% y frase completa)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrentCorrect) SuccessGreen else ErrorRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isCurrentCorrect) {
                            correctCount++
                        }
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            writtenInput = ""
                            selectedOptionIndex = null
                            isAnswerChecked = false
                            isCurrentCorrect = false
                            feedbackText = ""
                            recognizedSpokenText = ""
                            speechSimilarityPercentage = 0
                            speechErrorMessage = null
                        } else {
                            isFinished = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentCorrect) PrimaryGreen else ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else if (!isSpeakingType) {
                // Opción B: Botón Comprobar para Writing y Listening con tolerancia de "Casi correcto" (1 letra)
                Button(
                    onClick = {
                        val targetText = if (isListeningType) {
                            (currentQuestion.promptAudioText ?: currentQuestion.correctAnswer).ifBlank { currentQuestion.prompt }
                        } else {
                            currentQuestion.correctAnswer.ifBlank { currentQuestion.prompt }
                        }
                        val writtenEval = SpeechManager.evaluateWrittenAnswer(writtenInput, targetText)
                        isCurrentCorrect = writtenEval.isPassed
                        feedbackText = writtenEval.typoNotice ?: ""
                        isAnswerChecked = true
                        if (!writtenEval.isPassed) {
                            onWrongAnswer?.invoke(currentQuestion.id, writtenInput)
                        }
                    },
                    enabled = writtenInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Comprobar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PracticeResultView(
    skillMode: PracticeSkillMode,
    correctCount: Int,
    totalCount: Int,
    scorePercent: Int,
    onFinish: () -> Unit
) {
    Scaffold(
        containerColor = IndigoBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (scorePercent >= 70) "🎉" else "💪",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (scorePercent >= 70) "¡Sesión Completada con Éxito!" else "¡Buen Entrenamiento!",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = TextLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Entrenaste ${when(skillMode) {
                    PracticeSkillMode.SPEAKING -> "Speaking (Pronunciación)"
                    PracticeSkillMode.WRITING -> "Writing (Escritura)"
                    PracticeSkillMode.LISTENING -> "Listening (Comprensión Auditiva)"
                    PracticeSkillMode.MIX -> "Mix de Habilidades"
                }}",
                fontSize = 14.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = IndigoCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Aciertos", fontSize = 13.sp, color = TextMuted)
                        Text("$correctCount / $totalCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryCoral)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Precisión", fontSize = 13.sp, color = TextMuted)
                        Text("$scorePercent%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (scorePercent >= 70) AccentTeal else Color(0xFFF59E0B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Finalizar y Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
