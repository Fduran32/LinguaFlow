package com.linguaflow.app.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.ui.screens.path.VocabularyItem
import com.linguaflow.app.ui.theme.*
import com.linguaflow.app.util.SoundAndHapticsManager

enum class PracticeSkillMode {
    SPEAKING,
    WRITING,
    LISTENING,
    MIX
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeHubScreen(
    languageId: String = "en",
    languageName: String,
    flagEmoji: String,
    currentCefr: String,
    pendingMistakesCount: Int = 0,
    vocabularyItems: List<VocabularyItem> = emptyList(),
    onPlayAudio: ((text: String, isSlow: Boolean) -> Unit)? = null,
    onStartPractice: (PracticeSkillMode) -> Unit,
    onStartMistakesReview: () -> Unit = {},
    onBack: () -> Unit
) {
    var showGlossaryDialog by remember { mutableStateOf(false) }
    var showRoleplayDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de Entrenamiento", fontWeight = FontWeight.Bold, color = TextLight) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoBackground,
                    titleContentColor = TextLight,
                    navigationIconContentColor = TextLight
                )
            )
        },
        containerColor = IndigoBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de Nivel Activo
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = flagEmoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Idioma: $languageName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextLight
                        )
                        Text(
                            text = "Entrenando contenido del nivel:",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryCoral.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = currentCefr,
                            color = PrimaryCoral,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Opción D: Tarjeta de Repaso Inteligente de Errores (Repetición Espaciada / SRS)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pendingMistakesCount > 0) ErrorRedLight else IndigoCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (pendingMistakesCount > 0) ErrorRed.copy(alpha = 0.4f) else CardBorder
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = pendingMistakesCount > 0) { onStartMistakesReview() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (pendingMistakesCount > 0) ErrorRed.copy(alpha = 0.15f) else AccentTeal.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🧠", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Repaso Inteligente (SRS)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextLight
                                )
                                Text(
                                    text = if (pendingMistakesCount > 0) {
                                        "Tienes $pendingMistakesCount ejercicio(s) por dominar"
                                    } else {
                                        "¡0 errores pendientes! Estás al día 🎉"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (pendingMistakesCount > 0) ErrorRed else PrimaryGreenDark
                                )
                            }
                        }
                    }

                    if (pendingMistakesCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onStartMistakesReview,
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Dominar mis $pendingMistakesCount errores ahora",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Opción 2: Glosario Interactivo / Flashcards con Audio por Nivel
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal.copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGlossaryDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AccentTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📚", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mi Vocabulario por Nivel ($currentCefr)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextLight
                            )
                            Text(
                                text = "Explora frases clave con audio normal 🔊 y lento 🐢",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { showGlossaryDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentTeal.copy(alpha = 0.2f),
                            contentColor = PrimaryGreenDark
                        )
                    ) {
                        Text("Abrir", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Opción 3 (Fase 3): Simulador de Situaciones de la Vida Real (Viajes, Restaurante y Trabajo)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCoral.copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRoleplayDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryCoral.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "☕", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Situaciones de la Vida Real",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextLight
                            )
                            Text(
                                text = "Mini-diálogos interactivos: Restaurante, Viajes y Trabajo",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { showRoleplayDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PrimaryCoral.copy(alpha = 0.2f),
                            contentColor = PrimaryCoral
                        )
                    ) {
                        Text("Practicar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Selecciona la habilidad que deseas reforzar:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. SPEAKING
            SkillPracticeCard(
                emoji = "🗣️",
                title = "Speaking (Pronunciación)",
                description = "Usa tu voz con el micrófono para pronunciar oraciones completas y recibir evaluación de dicción al instante.",
                accentColor = AccentTeal,
                buttonText = "Comenzar Speaking",
                onClick = { onStartPractice(PracticeSkillMode.SPEAKING) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. WRITING
            SkillPracticeCard(
                emoji = "✍️",
                title = "Writing (Escritura)",
                description = "Fortalece tu ortografía y gramática con pistas interactivas y tolerancia inteligente de tipeo.",
                accentColor = PrimaryCoral,
                buttonText = "Comenzar Writing",
                onClick = { onStartPractice(PracticeSkillMode.WRITING) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. LISTENING
            SkillPracticeCard(
                emoji = "🎧",
                title = "Listening (Comprensión Auditiva)",
                description = "Entrena tu oído con pronunciaciones nativas a velocidad normal (1.0x) y lenta (tortuga 🐢 0.65x).",
                accentColor = AccentYellow,
                buttonText = "Comenzar Listening",
                onClick = { onStartPractice(PracticeSkillMode.LISTENING) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. MIX COMPLETO
            SkillPracticeCard(
                emoji = "🔀",
                title = "Mix de Habilidades",
                description = "Sesión completa de 10 ejercicios variados combinando habla, escucha y escritura a tu nivel.",
                accentColor = Color(0xFF818CF8),
                buttonText = "Comenzar Mix",
                onClick = { onStartPractice(PracticeSkillMode.MIX) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showGlossaryDialog) {
        VocabularyGlossaryDialog(
            languageName = languageName,
            flagEmoji = flagEmoji,
            currentCefr = currentCefr,
            vocabularyItems = vocabularyItems,
            onPlayAudio = onPlayAudio,
            onDismiss = { showGlossaryDialog = false }
        )
    }

    if (showRoleplayDialog) {
        RealLifeDialogueDialog(
            languageId = languageId,
            languageName = languageName,
            flagEmoji = flagEmoji,
            onPlayAudio = onPlayAudio,
            onDismiss = { showRoleplayDialog = false }
        )
    }
}

@Composable
private fun VocabularyGlossaryDialog(
    languageName: String,
    flagEmoji: String,
    currentCefr: String,
    vocabularyItems: List<VocabularyItem>,
    onPlayAudio: ((text: String, isSlow: Boolean) -> Unit)?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sublevels = remember(vocabularyItems) {
        listOf("Todos") + vocabularyItems.map { it.sublevelCefr }.distinct()
    }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var isFlashcardMode by remember { mutableStateOf(false) }
    var cardIndex by remember(selectedFilter) { mutableStateOf(0) }
    var isCardFlipped by remember(cardIndex, selectedFilter) { mutableStateOf(false) }
    var masteredCount by remember(selectedFilter) { mutableStateOf(0) }

    val filteredItems = remember(vocabularyItems, selectedFilter) {
        if (selectedFilter == "Todos") vocabularyItems
        else vocabularyItems.filter { it.sublevelCefr == selectedFilter }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IndigoCard,
        titleContentColor = TextLight,
        textContentColor = TextLight,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$flagEmoji 📚", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Vocabulario $languageName ($currentCefr)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TextLight
                        )
                        Text(
                            text = "${filteredItems.size} frases clave con pronunciación",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Opción 1 (Fase 3): Selector entre Vista Lista y Modo Flashcards Interactivas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isFlashcardMode) AccentTeal else IndigoSurfaceLight,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isFlashcardMode = false }
                    ) {
                        Text(
                            text = "📋 Vista Lista",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (!isFlashcardMode) IndigoBackground else TextLight,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isFlashcardMode) PrimaryCoral else IndigoSurfaceLight,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isFlashcardMode = true }
                    ) {
                        Text(
                            text = "🃏 Flashcards",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isFlashcardMode) Color.White else TextLight,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Filtros por subnivel (Todos, A1.1, A1.2, etc.)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sublevels.forEach { sub ->
                        val isSelected = selectedFilter == sub
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryCoral else IndigoSurfaceLight,
                            modifier = Modifier.clickable { selectedFilter = sub }
                        ) {
                            Text(
                                text = sub,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isFlashcardMode && filteredItems.isNotEmpty()) {
                    val currentCard = filteredItems[cardIndex.coerceIn(0, filteredItems.lastIndex)]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tarjeta ${cardIndex + 1} de ${filteredItems.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = "✅ Dominadas: $masteredCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tarjeta Flashcard Interactiva (Toca para girar)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = IndigoSurfaceLight,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isCardFlipped) AccentTeal else PrimaryCoral
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCardFlipped = !isCardFlipped }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryCoral.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = currentCard.sublevelCefr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryCoral,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = currentCard.targetPhrase,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = TextLight,
                                    textAlign = TextAlign.Center
                                )

                                if (!currentCard.phonetic.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Pinyin: ${currentCard.phonetic}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryCoral
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = { onPlayAudio?.invoke(currentCard.targetPhrase, false) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("🔊 Normal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onPlayAudio?.invoke(currentCard.targetPhrase, true) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("🐢 Lento", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (isCardFlipped) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AccentTeal.copy(alpha = 0.15f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Significado en Español:",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = currentCard.spanishMeaning,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryGreenDark,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "👆 Toca la tarjeta para revelar el significado en español",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentTeal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isCardFlipped = false
                                    cardIndex = (cardIndex + 1) % filteredItems.size
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🔄 Repetir luego", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    SoundAndHapticsManager.playCorrect(context)
                                    masteredCount++
                                    isCardFlipped = false
                                    cardIndex = (cardIndex + 1) % filteredItems.size
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("✅ Ya me la sé", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = IndigoSurfaceLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PrimaryCoral.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = item.sublevelCefr,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryCoral,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = AccentTeal.copy(alpha = 0.2f),
                                                modifier = Modifier.clickable {
                                                    onPlayAudio?.invoke(item.targetPhrase, false)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Audio normal",
                                                        tint = PrimaryGreenDark,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("1.0x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreenDark)
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = IndigoCard,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                                modifier = Modifier.clickable {
                                                    onPlayAudio?.invoke(item.targetPhrase, true)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🐢", fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("0.65x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.targetPhrase,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextLight
                                    )

                                    if (!item.phonetic.isNullOrBlank()) {
                                        Text(
                                            text = "Pinyin: ${item.phonetic}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryCoral
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = item.spanishMeaning,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cerrar Glosario", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}

private data class DialogueTurn(
    val npcLine: String,
    val npcSpanish: String,
    val options: List<String>,
    val correctReply: String,
    val replyMeaning: String
)

private data class RoleplayScenario(
    val id: String,
    val emoji: String,
    val title: String,
    val turns: List<DialogueTurn>
)

@Composable
private fun RealLifeDialogueDialog(
    languageId: String,
    languageName: String,
    flagEmoji: String,
    onPlayAudio: ((text: String, isSlow: Boolean) -> Unit)?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scenarios = remember(languageId) { buildRoleplayScenarios(languageId) }
    var selectedScenarioIndex by remember { mutableStateOf(0) }
    var currentTurnIndex by remember(selectedScenarioIndex) { mutableStateOf(0) }
    var selectedReply by remember(selectedScenarioIndex, currentTurnIndex) { mutableStateOf<String?>(null) }
    var isCompleted by remember(selectedScenarioIndex) { mutableStateOf(false) }

    val scenario = scenarios[selectedScenarioIndex]
    val turn = scenario.turns.getOrNull(currentTurnIndex)

    LaunchedEffect(selectedScenarioIndex, currentTurnIndex) {
        if (!isCompleted && turn != null) {
            onPlayAudio?.invoke(turn.npcLine, false)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IndigoCard,
        titleContentColor = TextLight,
        textContentColor = TextLight,
        title = {
            Column {
                Text(
                    text = "$flagEmoji Situaciones Reales ($languageName)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scenarios.forEachIndexed { idx, sc ->
                        val active = idx == selectedScenarioIndex
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) PrimaryCoral else IndigoSurfaceLight,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedScenarioIndex = idx }
                        ) {
                            Text(
                                text = "${sc.emoji} ${sc.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else TextLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            if (isCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "¡Conversación Completada!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = PrimaryGreenDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dominaste el diálogo de ${scenario.title} en $languageName con naturalidad.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            currentTurnIndex = 0
                            isCompleted = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Practicar otra vez", fontWeight = FontWeight.Bold, color = IndigoBackground)
                    }
                }
            } else if (turn != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Turno ${currentTurnIndex + 1} de ${scenario.turns.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Burbuja del interlocutor nativo
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = IndigoSurfaceLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${scenario.emoji} Interlocutor nativo:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreenDark
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AccentTeal.copy(alpha = 0.2f),
                                        modifier = Modifier.clickable { onPlayAudio?.invoke(turn.npcLine, false) }
                                    ) {
                                        Text(
                                            text = "🔊 1.0x",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreenDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = IndigoCard,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                        modifier = Modifier.clickable { onPlayAudio?.invoke(turn.npcLine, true) }
                                    ) {
                                        Text(
                                            text = "🐢 0.65x",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"${turn.npcLine}\"",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "(${turn.npcSpanish})",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Elige tu respuesta natural en $languageName:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    turn.options.forEach { option ->
                        val isPicked = selectedReply == option
                        val isRight = option == turn.correctReply
                        val bgColor = when {
                            !isPicked -> IndigoSurfaceLight
                            isRight -> SuccessGreenLight
                            else -> ErrorRedLight
                        }
                        val borderColor = when {
                            !isPicked -> CardBorder
                            isRight -> PrimaryGreen
                            else -> ErrorRed
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bgColor,
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedReply = option
                                    onPlayAudio?.invoke(option, false)
                                    if (option == turn.correctReply) {
                                        SoundAndHapticsManager.playCorrect(context)
                                    } else {
                                        SoundAndHapticsManager.playWrong(context)
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextLight
                                )
                                if (isPicked && isRight) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "✅ ${turn.replyMeaning}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreenDark
                                    )
                                }
                            }
                        }
                    }

                    if (selectedReply == turn.correctReply) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (currentTurnIndex + 1 < scenario.turns.size) {
                                    currentTurnIndex++
                                } else {
                                    SoundAndHapticsManager.playCelebration(context)
                                    isCompleted = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentTurnIndex + 1 < scenario.turns.size) "Siguiente turno →" else "¡Completar diálogo!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", fontWeight = FontWeight.Bold, color = PrimaryCoral)
            }
        }
    )
}

private fun buildRoleplayScenarios(languageId: String): List<RoleplayScenario> {
    return when (languageId) {
        "it" -> listOf(
            RoleplayScenario(
                id = "cafe",
                emoji = "☕",
                title = "Cafetería",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Buongiorno! Cosa desidera ordinare oggi?",
                        npcSpanish = "¡Buenos días! ¿Qué desea pedir hoy?",
                        options = listOf(
                            "Vorrei un cappuccino e un cornetto, per favore.",
                            "Io sono alla stazione dei treni.",
                            "Arrivederci a domani mattina."
                        ),
                        correctReply = "Vorrei un cappuccino e un cornetto, per favore.",
                        replyMeaning = "Quisiera un capuchino y un croissant, por favor."
                    ),
                    DialogueTurn(
                        npcLine = "Subito! Lo preferisce al banco o al tavolo?",
                        npcSpanish = "¡Enseguida! ¿Lo prefiere en la barra o en la mesa?",
                        options = listOf(
                            "Al tavolo vicino alla finestra, grazie.",
                            "Non parlo bene il cinese.",
                            "Ieri ha piovuto tutto il giorno."
                        ),
                        correctReply = "Al tavolo vicino alla finestra, grazie.",
                        replyMeaning = "En la mesa junto a la ventana, gracias."
                    )
                )
            ),
            RoleplayScenario(
                id = "travel",
                emoji = "✈️",
                title = "Viajes",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Buonasera, ha una prenotazione nel nostro albergo?",
                        npcSpanish = "Buenas noches, ¿tiene una reserva en nuestro hotel?",
                        options = listOf(
                            "Sì, ho prenotato una camera singola per tre notti.",
                            "Il treno parte dal binario quattro.",
                            "Vorrei pagare il conto del ristorante."
                        ),
                        correctReply = "Sì, ho prenotato una camera singola per tre notti.",
                        replyMeaning = "Sí, reservé una habitación individual por tres noches."
                    ),
                    DialogueTurn(
                        npcLine = "Perfetto! A che ora desidera la colazione domani?",
                        npcSpanish = "¡Perfecto! ¿A qué hora desea el desayuno mañana?",
                        options = listOf(
                            "Alle otto del mattino, grazie mille.",
                            "La mia valigia è molto pesante.",
                            "Abito a Roma da due anni."
                        ),
                        correctReply = "Alle otto del mattino, grazie mille.",
                        replyMeaning = "A las ocho de la mañana, muchas gracias."
                    )
                )
            ),
            RoleplayScenario(
                id = "work",
                emoji = "🤝",
                title = "Trabajo",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Benvenuto! Mi parli un po' della sua esperienza professionale.",
                        npcSpanish = "¡Bienvenido! Hábleme un poco de su experiencia profesional.",
                        options = listOf(
                            "Lavoro nello sviluppo software da tre anni.",
                            "Mi piace molto la cucina italiana.",
                            "Il museo apre alle nove."
                        ),
                        correctReply = "Lavoro nello sviluppo software da tre anni.",
                        replyMeaning = "Trabajo en desarrollo de software desde hace tres años."
                    ),
                    DialogueTurn(
                        npcLine = "Ottimo! Quando potrebbe iniziare a collaborare con noi?",
                        npcSpanish = "¡Excelente! ¿Cuándo podría empezar a colaborar con nosotros?",
                        options = listOf(
                            "Sono disponibile a partire dal prossimo lunedì.",
                            "Ho comprato un biglietto di andata e ritorno.",
                            "Fa molto caldo in estate."
                        ),
                        correctReply = "Sono disponibile a partire dal prossimo lunedì.",
                        replyMeaning = "Estoy disponible a partir del próximo lunes."
                    )
                )
            )
        )
        "zh" -> listOf(
            RoleplayScenario(
                id = "cafe",
                emoji = "☕",
                title = "Restaurante",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "你好！请问你想喝点什么？",
                        npcSpanish = "¡Hola! ¿Qué te gustaría beber?",
                        options = listOf(
                            "我想喝一杯热咖啡，谢谢。",
                            "我明天去北京旅游。",
                            "这本书非常有意思。"
                        ),
                        correctReply = "我想喝一杯热咖啡，谢谢。",
                        replyMeaning = "Me gustaría tomar una taza de café caliente, gracias."
                    ),
                    DialogueTurn(
                        npcLine = "好的，请问还需要别的东西吗？",
                        npcSpanish = "De acuerdo, ¿necesitas algo más?",
                        options = listOf(
                            "不用了，请问一共多少钱？",
                            "我是大学的学生。",
                            "今天天气非常好。"
                        ),
                        correctReply = "不用了，请问一共多少钱？",
                        replyMeaning = "Nada más, ¿cuánto cuesta en total, por favor?"
                    )
                )
            ),
            RoleplayScenario(
                id = "travel",
                emoji = "✈️",
                title = "Viajes",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "您好，请问您去哪个城市？",
                        npcSpanish = "Hola, ¿a qué ciudad viaja usted?",
                        options = listOf(
                            "我要去上海，这是我的护照。",
                            "我喜欢吃中国菜。",
                            "他们在学校学习汉语。"
                        ),
                        correctReply = "我要去上海，这是我的护照。",
                        replyMeaning = "Voy a Shanghái, este es mi pasaporte."
                    ),
                    DialogueTurn(
                        npcLine = "谢谢，您的登机口在十二号。",
                        npcSpanish = "Gracias, su puerta de embarque es la número 12.",
                        options = listOf(
                            "非常感谢您的帮助！",
                            "我昨天买了一件衣服。",
                            "现在是下午三点。"
                        ),
                        correctReply = "非常感谢您的帮助！",
                        replyMeaning = "¡Muchas gracias por su ayuda!"
                    )
                )
            ),
            RoleplayScenario(
                id = "work",
                emoji = "🤝",
                title = "Trabajo",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "很高兴认识你，请问你在哪儿工作？",
                        npcSpanish = "Mucho gusto en conocerte, ¿dónde trabajas?",
                        options = listOf(
                            "我在一家科技公司工作。",
                            "我想买两张电影票。",
                            "火车站离这儿很近。"
                        ),
                        correctReply = "我在一家科技公司工作。",
                        replyMeaning = "Trabajo en una empresa de tecnología."
                    ),
                    DialogueTurn(
                        npcLine = "太棒了！你每天几点开始上班？",
                        npcSpanish = "¡Genial! ¿A qué hora empiezas a trabajar cada día?",
                        options = listOf(
                            "我每天早上九点开始上班。",
                            "我不喜欢喝冰水。",
                            "这个房间很大很干净。"
                        ),
                        correctReply = "我每天早上九点开始上班。",
                        replyMeaning = "Empiezo a trabajar todos los días a las nueve de la mañana."
                    )
                )
            )
        )
        "pt" -> listOf(
            RoleplayScenario(
                id = "cafe",
                emoji = "☕",
                title = "Cafetería",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Bom dia! O que você gostaria de pedir hoje?",
                        npcSpanish = "¡Buenos días! ¿Qué le gustaría pedir hoy?",
                        options = listOf(
                            "Eu gostaria de um café com leite e um pão de queijo, por favor.",
                            "O trem sai às oito horas da manhã.",
                            "Eu moro no Brasil há dois anos."
                        ),
                        correctReply = "Eu gostaria de um café com leite e um pão de queijo, por favor.",
                        replyMeaning = "Me gustaría un café con leche y un pan de queso, por favor."
                    ),
                    DialogueTurn(
                        npcLine = "Com certeza! Você prefere pagar com cartão ou dinheiro?",
                        npcSpanish = "¡Por supuesto! ¿Prefiere pagar con tarjeta o efectivo?",
                        options = listOf(
                            "Vou pagar com cartão de crédito, obrigado.",
                            "A praia fica muito longe daqui.",
                            "Ontem choveu durante a tarde."
                        ),
                        correctReply = "Vou pagar com cartão de crédito, obrigado.",
                        replyMeaning = "Voy a pagar con tarjeta de crédito, gracias."
                    )
                )
            ),
            RoleplayScenario(
                id = "travel",
                emoji = "✈️",
                title = "Viajes",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Boa tarde! Você tem uma reserva no nosso hotel?",
                        npcSpanish = "¡Buenas tardes! ¿Tiene una reserva en nuestro hotel?",
                        options = listOf(
                            "Sim, reservei um quarto para duas pessoas.",
                            "Eu estudo português todos os dias.",
                            "O restaurante fecha às dez horas."
                        ),
                        correctReply = "Sim, reservei um quarto para duas pessoas.",
                        replyMeaning = "Sí, reservé una habitación para dos personas."
                    ),
                    DialogueTurn(
                        npcLine = "Perfeito! Aqui está a chave do seu quarto.",
                        npcSpanish = "¡Perfecto! Aquí está la llave de su habitación.",
                        options = listOf(
                            "Muito obrigado! A que horas é o café da manhã?",
                            "Eu preciso comprar uma passagem de ônibus.",
                            "Meu irmão trabalha no hospital."
                        ),
                        correctReply = "Muito obrigado! A que horas é o café da manhã?",
                        replyMeaning = "¡Muchas gracias! ¿A qué hora es el desayuno?"
                    )
                )
            ),
            RoleplayScenario(
                id = "work",
                emoji = "🤝",
                title = "Trabajo",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Seja bem-vindo! Fale um pouco sobre a sua profissão.",
                        npcSpanish = "¡Sea bienvenido! Hable un poco sobre su profesión.",
                        options = listOf(
                            "Eu sou desenvolvedor de software e adoro tecnologia.",
                            "Nós vamos viajar no próximo fim de semana.",
                            "Eu gostaria de reservar uma mesa."
                        ),
                        correctReply = "Eu sou desenvolvedor de software e adoro tecnologia.",
                        replyMeaning = "Soy desarrollador de software y me encanta la tecnología."
                    ),
                    DialogueTurn(
                        npcLine = "Excelente! Quando você pode começar o novo projeto?",
                        npcSpanish = "¡Excelente! ¿Cuándo puede comenzar el nuevo proyecto?",
                        options = listOf(
                            "Posso começar na próxima segunda-feira.",
                            "O aeroporto fica no centro da cidade.",
                            "Hoje o dia está muito bonito."
                        ),
                        correctReply = "Posso começar na próxima segunda-feira.",
                        replyMeaning = "Puedo empezar el próximo lunes."
                    )
                )
            )
        )
        else -> listOf(
            RoleplayScenario(
                id = "cafe",
                emoji = "☕",
                title = "Cafetería",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Good morning! What would you like to order today?",
                        npcSpanish = "¡Buenos días! ¿Qué te gustaría pedir hoy?",
                        options = listOf(
                            "I would like a hot coffee and a sandwich, please.",
                            "The train station is on the left.",
                            "I went to the cinema yesterday."
                        ),
                        correctReply = "I would like a hot coffee and a sandwich, please.",
                        replyMeaning = "Me gustaría un café caliente y un sándwich, por favor."
                    ),
                    DialogueTurn(
                        npcLine = "Sure! Would you like that for here or to go?",
                        npcSpanish = "¡Claro! ¿Lo quieres para tomar aquí o para llevar?",
                        options = listOf(
                            "For here, please. How much is it?",
                            "My brother lives in New York.",
                            "She is studying for the exam."
                        ),
                        correctReply = "For here, please. How much is it?",
                        replyMeaning = "Para tomar aquí, por favor. ¿Cuánto es?"
                    )
                )
            ),
            RoleplayScenario(
                id = "travel",
                emoji = "✈️",
                title = "Viajes",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Welcome! Do you have a reservation at our hotel?",
                        npcSpanish = "¡Bienvenido! ¿Tiene una reserva en nuestro hotel?",
                        options = listOf(
                            "Yes, I have a reservation under my name for three nights.",
                            "I usually wake up at seven o'clock.",
                            "Can you tell me the time, please?"
                        ),
                        correctReply = "Yes, I have a reservation under my name for three nights.",
                        replyMeaning = "Sí, tengo una reserva a mi nombre por tres noches."
                    ),
                    DialogueTurn(
                        npcLine = "Great! Here is your room key. Breakfast starts at seven.",
                        npcSpanish = "¡Genial! Aquí tiene su llave. El desayuno empieza a las siete.",
                        options = listOf(
                            "Thank you very much for your help!",
                            "I don't like rainy weather.",
                            "Where is the nearest hospital?"
                        ),
                        correctReply = "Thank you very much for your help!",
                        replyMeaning = "¡Muchas gracias por su ayuda!"
                    )
                )
            ),
            RoleplayScenario(
                id = "work",
                emoji = "🤝",
                title = "Trabajo",
                turns = listOf(
                    DialogueTurn(
                        npcLine = "Nice to meet you! Tell me a little about your background.",
                        npcSpanish = "¡Mucho gusto! Cuéntame un poco sobre tu experiencia.",
                        options = listOf(
                            "I have been working in software development for three years.",
                            "I bought a new book at the store.",
                            "We are going to the beach tomorrow."
                        ),
                        correctReply = "I have been working in software development for three years.",
                        replyMeaning = "Llevo tres años trabajando en desarrollo de software."
                    ),
                    DialogueTurn(
                        npcLine = "That sounds impressive! When would you be able to start?",
                        npcSpanish = "¡Suena impresionante! ¿Cuándo podrías empezar?",
                        options = listOf(
                            "I am available to start next Monday.",
                            "The flight leaves in two hours.",
                            "Please open the window."
                        ),
                        correctReply = "I am available to start next Monday.",
                        replyMeaning = "Estoy disponible para empezar el próximo lunes."
                    )
                )
            )
        )
    }
}

@Composable
private fun SkillPracticeCard(
    emoji: String,
    title: String,
    description: String,
    accentColor: Color,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextLight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    color = if (accentColor == AccentTeal || accentColor == AccentYellow) IndigoBackground else Color.White
                )
            }
        }
    }
}
