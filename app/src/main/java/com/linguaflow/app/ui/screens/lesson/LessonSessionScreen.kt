package com.linguaflow.app.ui.screens.lesson

import android.content.pm.PackageManager
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
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
import com.linguaflow.app.domain.model.AnswerResult
import com.linguaflow.app.domain.model.Question
import com.linguaflow.app.domain.model.QuestionType
import com.linguaflow.app.domain.usecase.EvaluateAnswerUseCase
import com.linguaflow.app.ui.theme.*
import com.linguaflow.app.util.SpeechManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LessonSessionScreen(
    levelTitle: String,
    questions: List<Question>,
    ttsLocaleStr: String = "en-US",
    onPlayAudio: ((text: String, locale: String) -> Unit)? = null,
    onWrongAnswer: ((questionId: String, wrongAnswer: String) -> Unit)? = null,
    onCorrectAnswer: ((questionId: String) -> Unit)? = null,
    onLessonFinished: (passed: Boolean, scorePercentage: Int) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var answerResult by remember { mutableStateOf<AnswerResult?>(null) }
    var correctCount by remember { mutableStateOf(0) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // Estado para ordenar palabras (TRANSLATE_BUILD)
    var sentenceTokens by remember(currentIndex) { mutableStateOf<List<String>>(emptyList()) }

    // Estado para reconocimiento de voz (SPEAKING)
    var spokenAnswer by remember(currentIndex) { mutableStateOf<String?>(null) }
    var isListeningSpeech by remember { mutableStateOf(false) }

    val evaluateUseCase = remember { EvaluateAnswerUseCase() }
    val currentQuestion = questions.getOrNull(currentIndex)

    val total = questions.size
    val scorePercentage = if (total > 0) (correctCount * 100) / total else 0
    val passed = scorePercentage >= 80

    // Motor nativo integrado de voz (SpeechManager in-app sin Google Dialog)
    val speechManager = remember { SpeechManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
        }
    }

    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lessonMicPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListeningSpeech) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Auto-reproducir audio si es LISTENING
    LaunchedEffect(currentQuestion?.id) {
        if (currentQuestion?.type == QuestionType.LISTENING && !currentQuestion.promptAudioText.isNullOrEmpty()) {
            onPlayAudio?.invoke(currentQuestion.promptAudioText, ttsLocaleStr)
        }
    }

    val progress = if (total > 0) (currentIndex.toFloat()) / total.toFloat() else 0f

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDark)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = PrimaryGreen,
                    trackColor = CardBorder
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${minOf(currentIndex + 1, total)}/$total",
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
            }
        },
        bottomBar = {
            if (answerResult != null) {
                val result = answerResult!!
                val isSuccess = result.isCorrect

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSuccess) SuccessGreenLight else ErrorRedLight)
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (isSuccess) "🎉 ¡EXCELENTE!" else "❌ RESPUESTA INCORRECTA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) PrimaryGreenDark else ErrorRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isSuccess) {
                        Text(
                            text = "Solución correcta: ${result.correctAnswer}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 Explicación: ${result.explanation}",
                            fontSize = 14.sp,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (result.isCorrect) {
                                correctCount++
                                onCorrectAnswer?.invoke(currentQuestion?.id ?: "")
                            }
                            answerResult = null
                            selectedOption = null
                            sentenceTokens = emptyList()
                            spokenAnswer = null

                            if (currentIndex + 1 < questions.size) {
                                currentIndex++
                            } else {
                                showCompletionDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSuccess) PrimaryGreen else ErrorRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isSuccess) "CONTINUAR" else "ENTENDIDO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (currentQuestion != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val isActionEnabled = when (currentQuestion.type) {
                        QuestionType.TRANSLATE_BUILD -> sentenceTokens.isNotEmpty()
                        QuestionType.SPEAKING -> !spokenAnswer.isNullOrEmpty()
                        else -> selectedOption != null
                    }

                    Button(
                        onClick = {
                            val answerToValidate = when (currentQuestion.type) {
                                QuestionType.TRANSLATE_BUILD -> sentenceTokens.joinToString(" ")
                                QuestionType.SPEAKING -> spokenAnswer ?: ""
                                else -> selectedOption ?: ""
                            }
                            val res = evaluateUseCase.execute(currentQuestion, answerToValidate)
                            answerResult = res
                            if (!res.isCorrect) {
                                onWrongAnswer?.invoke(currentQuestion.id, answerToValidate)
                            }
                        },
                        enabled = isActionEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = LockedGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("COMPROBAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = levelTitle,
                    fontSize = 14.sp,
                    color = PrimaryGreenDark,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Enunciado
                Text(
                    text = currentQuestion.prompt,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (!currentQuestion.promptPhonetic.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pinyin: ${currentQuestion.promptPhonetic}",
                        fontSize = 16.sp,
                        color = PrimaryGreenDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Renderizado Especial por Tipo de Ejercicio
                when (currentQuestion.type) {
                    QuestionType.SPEAKING -> {
                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IndigoCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Frase a pronunciar:",
                                    fontSize = 14.sp,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentQuestion.correctAnswer,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreenDark,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Botón de referencia auditiva (Shadowing: escuchar modelo nativo antes de hablar)
                                OutlinedButton(
                                    onClick = {
                                        val audioToPlay = currentQuestion.promptAudioText ?: currentQuestion.correctAnswer
                                        onPlayAudio?.invoke(audioToPlay, ttsLocaleStr)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Escuchar referencia",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Escuchar pronunciación", fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Botón de Micrófono Integrado (100% in-app, sin Google Dialog)
                                Box(
                                    modifier = Modifier.size(110.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isListeningSpeech) {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .scale(pulseScale)
                                                .clip(CircleShape)
                                                .background(PrimaryGreen.copy(alpha = 0.25f))
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .background(if (isListeningSpeech) ErrorRed else PrimaryGreen)
                                            .clickable {
                                                if (!hasRecordPermission) {
                                                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                                    return@clickable
                                                }

                                                if (isListeningSpeech) {
                                                    speechManager.stopListening()
                                                    isListeningSpeech = false
                                                } else {
                                                    spokenAnswer = null
                                                    speechManager.startListening(
                                                        localeStr = ttsLocaleStr,
                                                        onReady = { isListeningSpeech = true },
                                                        onPartialResult = { partial ->
                                                            spokenAnswer = partial
                                                            selectedOption = partial
                                                        },
                                                        onFinalResult = { finalResult, allCandidates ->
                                                            isListeningSpeech = false
                                                            val evaluation = SpeechManager.calculateBestSimilarity(
                                                                allCandidates.ifEmpty { listOf(finalResult) },
                                                                currentQuestion.correctAnswer
                                                            )
                                                            spokenAnswer = evaluation.bestSpoken.ifBlank { finalResult }
                                                            selectedOption = spokenAnswer
                                                        },
                                                        onError = { errorMsg ->
                                                            isListeningSpeech = false
                                                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isListeningSpeech) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = "Micrófono",
                                            tint = Color.White,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isListeningSpeech) "Escuchando... habla ahora" else "Toca el micrófono y pronuncia la frase",
                                    fontSize = 14.sp,
                                    color = if (isListeningSpeech) ErrorRed else TextGray,
                                    fontWeight = FontWeight.Medium
                                )

                                if (!spokenAnswer.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SuccessGreenLight
                                    ) {
                                        Text(
                                            text = "Te escuchamos: \"$spokenAnswer\"",
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreenDark,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    QuestionType.LISTENING -> {
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPlayAudio?.invoke(currentQuestion.promptAudioText ?: currentQuestion.correctAnswer, ttsLocaleStr)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SuccessGreenLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Hearing,
                                    contentDescription = "Escuchar",
                                    tint = PrimaryGreenDark,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Tocar para escuchar audio",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreenDark,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Opciones barajadas aleatoriamente para evitar que la primera sea siempre la correcta
                        val shuffledListeningOptions = remember(currentQuestion.id, currentIndex) {
                            currentQuestion.options.shuffled()
                        }

                        shuffledListeningOptions.forEach { option ->
                            RenderOptionCard(
                                option = option,
                                isSelected = selectedOption == option,
                                isEnabled = answerResult == null,
                                onSelect = { selectedOption = option }
                            )
                        }
                    }

                    QuestionType.TRANSLATE_BUILD -> {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Zona de Construcción
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, if (sentenceTokens.isNotEmpty()) PrimaryCoral else CardBorder, RoundedCornerShape(12.dp))
                                .background(IndigoCard)
                                .padding(12.dp)
                        ) {
                            if (sentenceTokens.isEmpty()) {
                                Text(
                                    text = "Toca las palabras de abajo para armar la frase...",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    sentenceTokens.forEachIndexed { tokenIndex, word ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryCoral,
                                            modifier = Modifier.clickable(enabled = answerResult == null) {
                                                sentenceTokens = sentenceTokens.toMutableList().also { it.removeAt(tokenIndex) }
                                            }
                                        ) {
                                            Text(
                                                text = word,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Banco de palabras:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Banco de Fichas barajado aleatoriamente para evitar que aparezcan en la misma posición
                        val shuffledWordBank = remember(currentQuestion.id, currentIndex) {
                            currentQuestion.options.shuffled()
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            shuffledWordBank.forEach { word ->
                                val usedCount = sentenceTokens.count { it == word }
                                val availableInPool = currentQuestion.options.count { it == word }
                                val isAvailable = usedCount < availableInPool

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isAvailable) IndigoSurfaceLight else LockedGray.copy(alpha = 0.3f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isAvailable) CardBorder else Color.Transparent),
                                    modifier = Modifier.clickable(enabled = isAvailable && answerResult == null) {
                                        sentenceTokens = sentenceTokens + word
                                    }
                                ) {
                                    Text(
                                        text = word,
                                        color = if (isAvailable) TextLight else Color.Transparent,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // MULTIPLE_CHOICE y FILL_BLANK
                        if (!currentQuestion.promptAudioText.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SuccessGreenLight)
                                    .clickable {
                                        onPlayAudio?.invoke(currentQuestion.promptAudioText, ttsLocaleStr)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Audio", tint = PrimaryGreenDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Escuchar pronunciación",
                                    color = PrimaryGreenDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Opciones barajadas aleatoriamente cada vez que se presenta la pregunta
                        val shuffledOptions = remember(currentQuestion.id, currentIndex) {
                            currentQuestion.options.shuffled()
                        }

                        shuffledOptions.forEach { option ->
                            RenderOptionCard(
                                option = option,
                                isSelected = selectedOption == option,
                                isEnabled = answerResult == null,
                                onSelect = { selectedOption = option }
                            )
                        }
                    }
                }
            }
        }

        // Diálogo Final de Evaluación
        if (showCompletionDialog) {
            AlertDialog(
                onDismissRequest = {},
                icon = {
                    Icon(
                        if (passed) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = if (passed) "Aprobado" else "No superado",
                        tint = if (passed) PrimaryGreen else ErrorRed,
                        modifier = Modifier.size(52.dp)
                    )
                },
                title = {
                    Text(
                        text = if (passed) "🎉 ¡Nivel Superado!" else "⚠️ Nivel No Superado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Puntaje obtenido: $scorePercentage% ($correctCount de $total aciertos)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (passed) {
                                "¡Excelente trabajo! Has completado todas las competencias del nivel ($scorePercentage%) y desbloqueado el siguiente nivel."
                            } else {
                                "Para desbloquear el siguiente nivel necesitas al menos un 80% de aciertos. ¡Revisa las explicaciones e inténtalo de nuevo!"
                            },
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCompletionDialog = false
                            if (passed) {
                                onLessonFinished(true, scorePercentage)
                            } else {
                                // Si reprobó, reiniciamos la sesión del nivel actual
                                currentIndex = 0
                                correctCount = 0
                                selectedOption = null
                                answerResult = null
                                sentenceTokens = emptyList()
                                spokenAnswer = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (passed) PrimaryGreen else ErrorRed
                        )
                    ) {
                        Text(if (passed) "Continuar a la ruta" else "Reintentar nivel", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    if (!passed) {
                        TextButton(
                            onClick = {
                                showCompletionDialog = false
                                onLessonFinished(false, scorePercentage)
                            }
                        ) {
                            Text("Volver a la ruta", color = TextGray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RenderOptionCard(
    option: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryCoral else CardBorder
    val bgColor = if (isSelected) PrimaryCoral.copy(alpha = 0.2f) else IndigoCard

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = isEnabled) { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (isSelected) 2.dp else 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryCoral)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = option,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextLight
            )
        }
    }
}
