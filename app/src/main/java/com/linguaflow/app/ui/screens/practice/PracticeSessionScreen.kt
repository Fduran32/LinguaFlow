package com.linguaflow.app.ui.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
        // PANTALLA DE RESULTADOS
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
    val isSpeakingType = skillMode == PracticeSkillMode.SPEAKING || (skillMode == PracticeSkillMode.MIX && currentIndex % 3 == 0)
    val isListeningType = skillMode == PracticeSkillMode.LISTENING || (skillMode == PracticeSkillMode.MIX && currentIndex % 3 == 1)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDark)
                    }

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = PrimaryGreen,
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${currentIndex + 1}/${questions.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMedium
                    )
                }
            }
        },
        containerColor = Color(0xFFF7F9FA)
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
                val targetText: String = currentQuestion.correctAnswer.ifBlank { currentQuestion.prompt }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            text = "Pronuncia la siguiente frase en inglés:",
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
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
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
                        // Anillo de pulso exterior
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.2f))
                        )
                    }

                    // Botón principal
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
                    text = if (isListening) "🎙️ Escuchando... habla en inglés" else "Toca el micrófono y pronuncia la frase",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isListening) PrimaryGreenDark else TextMedium
                )

                if (recognizedSpokenText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Tu voz detectada:", fontSize = 12.sp, color = TextMedium)
                            Text(
                                text = "\"$recognizedSpokenText\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
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
                val spokenPhrase: String = currentQuestion.correctAnswer.ifBlank { currentQuestion.prompt }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { onPlayAudio(spokenPhrase, false) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reproducir", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onPlayAudio(spokenPhrase, true) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Text(text = "🐢", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Audio Lento", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "¿Qué frase o palabra escuchaste?",
                            fontSize = 14.sp,
                            color = TextMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = writtenInput,
                            onValueChange = { writtenInput = it },
                            placeholder = { Text("Escribe en inglés lo que escuchas...") },
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Traduce la siguiente frase al inglés:",
                            fontSize = 14.sp,
                            color = TextMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentQuestion.prompt,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreenDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = writtenInput,
                            onValueChange = { writtenInput = it },
                            placeholder = { Text("Escribe aquí tu respuesta...") },
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCurrentCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCurrentCorrect) PrimaryGreenDark else ErrorRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCurrentCorrect) "¡Excelente!" else "Respuesta correcta:",
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentCorrect) PrimaryGreenDark else ErrorRed,
                                fontSize = 16.sp
                            )
                        }

                        if (!isCurrentCorrect) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.correctAnswer.ifBlank { currentQuestion.prompt },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )
                        }

                        if (feedbackText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = feedbackText,
                                fontSize = 13.sp,
                                color = TextDark
                            )
                        }

                        if (speechSimilarityPercentage > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Similitud fonética: $speechSimilarityPercentage%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrentCorrect) PrimaryGreenDark else ErrorRed
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
                // Botón Comprobar para Writing y Listening
                Button(
                    onClick = {
                        fun normalize(text: String): String {
                            return text.lowercase()
                                .replace("she's", "she is")
                                .replace("he's", "he is")
                                .replace("it's", "it is")
                                .replace("i'm", "i am")
                                .replace("you're", "you are")
                                .replace("we're", "we are")
                                .replace("they're", "they are")
                                .replace("that's", "that is")
                                .replace("don't", "do not")
                                .replace("doesn't", "does not")
                                .replace("didn't", "did not")
                                .replace("can't", "cannot")
                                .replace("isn't", "is not")
                                .replace("aren't", "are not")
                                .replace(Regex("[.,?!¿¡:;\"']"), "")
                                .replace(Regex("\\s+"), " ")
                                .trim()
                        }
                        val cleanInput = normalize(writtenInput)
                        val target = normalize(currentQuestion.correctAnswer.ifBlank { currentQuestion.prompt })
                        isCurrentCorrect = cleanInput == target
                        isAnswerChecked = true
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
        containerColor = Color.White
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
                color = TextDark,
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
                color = TextMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF3F4F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Aciertos", fontSize = 13.sp, color = TextMedium)
                        Text("$correctCount / $totalCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Precisión", fontSize = 13.sp, color = TextMedium)
                        Text("$scorePercent%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (scorePercent >= 70) PrimaryGreenDark else Color(0xFFF59E0B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
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
