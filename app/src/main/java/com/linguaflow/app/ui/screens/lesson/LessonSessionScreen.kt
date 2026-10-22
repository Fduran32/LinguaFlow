package com.linguaflow.app.ui.screens.lesson

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.domain.model.AnswerResult
import com.linguaflow.app.domain.model.Question
import com.linguaflow.app.domain.model.QuestionType
import com.linguaflow.app.domain.usecase.EvaluateAnswerUseCase
import com.linguaflow.app.ui.theme.*
import java.util.Locale

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

    // Lanzador nativo de reconocimiento de voz (Speech-to-Text)
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningSpeech = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                spokenAnswer = spokenText
                selectedOption = spokenText
            }
        }
    }

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
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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
                            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    color = PrimaryGreenDark
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Botón de Micrófono
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(if (isListeningSpeech) ErrorRed else PrimaryGreen)
                                        .clickable {
                                            try {
                                                isListeningSpeech = true
                                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, ttsLocaleStr)
                                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
                                                }
                                                speechRecognizerLauncher.launch(intent)
                                            } catch (e: Exception) {
                                                isListeningSpeech = false
                                                Toast.makeText(context, "Reconocimiento de voz no disponible", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Micrófono",
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
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
                                    onPlayAudio?.invoke(currentQuestion.promptAudioText ?: "", ttsLocaleStr)
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

                        currentQuestion.options.forEach { option ->
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
                                .border(1.dp, if (sentenceTokens.isNotEmpty()) PrimaryGreen else CardBorder, RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            if (sentenceTokens.isEmpty()) {
                                Text(
                                    text = "Toca las palabras de abajo para armar la frase...",
                                    color = TextGray,
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
                                            color = PrimaryGreen,
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
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Banco de Fichas
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentQuestion.options.forEach { word ->
                                val usedCount = sentenceTokens.count { it == word }
                                val availableInPool = currentQuestion.options.count { it == word }
                                val isAvailable = usedCount < availableInPool

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isAvailable) Color.White else LockedGray.copy(alpha = 0.3f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isAvailable) CardBorder else Color.Transparent),
                                    modifier = Modifier.clickable(enabled = isAvailable && answerResult == null) {
                                        sentenceTokens = sentenceTokens + word
                                    }
                                ) {
                                    Text(
                                        text = word,
                                        color = if (isAvailable) TextDark else Color.Transparent,
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

                        currentQuestion.options.forEach { option ->
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
                            onLessonFinished(passed, scorePercentage)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (passed) PrimaryGreen else ErrorRed
                        )
                    ) {
                        Text(if (passed) "Continuar a la ruta" else "Reintentar nivel", fontWeight = FontWeight.Bold)
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
    val borderColor = if (isSelected) PrimaryGreen else CardBorder
    val bgColor = if (isSelected) SuccessGreenLight.copy(alpha = 0.35f) else Color.White

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
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = option,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
        }
    }
}

