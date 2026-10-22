package com.linguaflow.app.ui.screens.path

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.domain.model.Language
import com.linguaflow.app.domain.model.Level
import com.linguaflow.app.domain.model.User
import com.linguaflow.app.ui.components.UserAvatar
import com.linguaflow.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningPathScreen(
    currentUser: User? = null,
    currentLanguageName: String,
    currentFlagEmoji: String,
    availableLanguages: List<Language> = emptyList(),
    levels: List<Level>,
    userAssignedLevel: String = "A1",
    selectedViewingLevel: String = "A1",
    availablePracticeLevels: List<String> = listOf("A1"),
    pendingMistakesCount: Int = 0,
    isLoading: Boolean = false,
    onSelectLanguage: (String) -> Unit = {},
    onSelectViewingLevel: (String) -> Unit = {},
    onOpenMistakesReview: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onStartLevel: (Level) -> Unit,
    onTakePlacementTest: () -> Unit
) {
    var lockedAlertMessage by remember { mutableStateOf<String?>(null) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (availableLanguages.isNotEmpty()) showLanguageMenu = true
                        }
                    ) {
                        Text(text = currentFlagEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = currentLanguageName, fontWeight = FontWeight.Bold)
                        if (availableLanguages.isNotEmpty()) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar idioma")
                        }
                    }

                    // Menú desplegable para cambiar entre Inglés, Italiano y Chino
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        availableLanguages.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = lang.flagEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = lang.name, fontWeight = FontWeight.Medium)
                                    }
                                },
                                onClick = {
                                    showLanguageMenu = false
                                    onSelectLanguage(lang.id)
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenMistakesReview) {
                        BadgedBox(
                            badge = {
                                if (pendingMistakesCount > 0) {
                                    Badge(containerColor = ErrorRed) {
                                        Text("$pendingMistakesCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = "Cuaderno de Errores",
                                tint = if (pendingMistakesCount > 0) ErrorRed else TextDark
                            )
                        }
                    }
                    TextButton(onClick = onTakePlacementTest) {
                        Text("Prueba de Nivel", color = PrimaryGreenDark, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onOpenProfile) {
                        if (currentUser != null) {
                            UserAvatar(
                                avatar = currentUser.avatar,
                                isCustomPhoto = currentUser.isCustomPhoto,
                                size = 34.dp,
                                textSize = 18.sp,
                                borderWidth = 1.5.dp
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Perfil")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (levels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(NeutralBackground),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedViewingLevel,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = if (selectedViewingLevel == userAssignedLevel) {
                                            "Nivel Actual: $userAssignedLevel"
                                        } else {
                                            "Repasando Nivel: $selectedViewingLevel"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = TextDark
                                    )
                                    Text(
                                        text = if (selectedViewingLevel == userAssignedLevel) {
                                            "Marco de Referencia Europeo (A1 a C2)"
                                        } else {
                                            "Nivel principal asignado: $userAssignedLevel"
                                        },
                                        fontSize = 13.sp,
                                        color = TextGray
                                    )
                                }
                            }

                            if (availablePracticeLevels.size > 1) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Practicar o repasar niveles disponibles:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    availablePracticeLevels.forEach { cefr ->
                                        val isSelected = cefr == selectedViewingLevel
                                        val isCurrent = cefr == userAssignedLevel
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onSelectViewingLevel(cefr) },
                                            label = {
                                                Text(
                                                    text = if (isCurrent) "$cefr (Actual)" else "$cefr (Repaso)",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = PrimaryGreen.copy(alpha = 0.15f),
                                                selectedLabelColor = PrimaryGreenDark
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                itemsIndexed(levels) { index, level ->
                    val isUnlocked = level.isUnlocked
                    val isExam = level.orderIndex == 11 || level.id.contains("exam")

                    LevelNodeItem(
                        level = level,
                        isUnlocked = isUnlocked,
                        onClick = {
                            if (isUnlocked) {
                                onStartLevel(level)
                            } else {
                                lockedAlertMessage = if (isExam) {
                                    "🔒 El Examen Final está bloqueado. Debes completar primero los 10 subniveles con al menos un 80% de aciertos para poder rendir la prueba de graduación."
                                } else {
                                    "🔒 Este subnivel está bloqueado. Debes completar primero el subnivel anterior con al menos un 80% de aciertos."
                                }
                            }
                        }
                    )

                    if (index < levels.size - 1) {
                        // Línea conectora entre subniveles: verde si el subnivel ya fue completado
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(36.dp)
                                .background(if (level.isCompleted) PrimaryGreenDark else CardBorder)
                        )
                    }
                }

                // Tarjeta bloqueada del siguiente nivel macro (ej. A2 bloqueado hasta completar A1.10)
                item {
                    val nextMacroCefr = when (userAssignedLevel.take(2).uppercase()) {
                        "A1" -> "Nivel A2"
                        "A2" -> "Nivel B1"
                        "B1" -> "Nivel B2"
                        "B2" -> "Nivel C1"
                        "C1" -> "Nivel C2"
                        else -> "Maestría Completa"
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(CardBorder)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Próximo desafío",
                                    tint = LockedGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Próximo desafío: $nextMacroCefr",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Bloqueado hasta completar los 10 subniveles de ${userAssignedLevel.take(2).uppercase()}",
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Diálogo de alerta si intenta saltarse niveles (RN-01)
        if (lockedAlertMessage != null) {
            AlertDialog(
                onDismissRequest = { lockedAlertMessage = null },
                icon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = ErrorRed,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(text = "Nivel Bloqueado", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(text = lockedAlertMessage ?: "")
                },
                confirmButton = {
                    Button(
                        onClick = { lockedAlertMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}

@Composable
fun LevelNodeItem(
    level: Level,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val isExam = level.orderIndex == 11 || level.id.contains("exam")
    val bgColor = when {
        level.isCompleted -> PrimaryGreen
        isUnlocked -> if (isExam) Color(0xFFFFB300) else AccentYellow
        else -> LockedGray
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(if (isExam) 84.dp else 76.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            when {
                level.isCompleted -> {
                    Icon(Icons.Default.Check, contentDescription = "Completado", tint = Color.White, modifier = Modifier.size(38.dp))
                }
                isUnlocked -> {
                    Icon(
                        if (isExam) Icons.Default.School else Icons.Default.PlayArrow,
                        contentDescription = "En curso",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
                else -> {
                    Icon(
                        if (isExam) Icons.Default.School else Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = level.title,
            fontWeight = FontWeight.Bold,
            fontSize = if (isExam) 17.sp else 16.sp,
            color = if (isUnlocked) TextDark else TextGray
        )
        if (isExam) {
            Text(
                text = "10 Preguntas (Escrita, Selección, Hablada y Escuchada)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnlocked) PrimaryGreenDark else TextGray
            )
        } else {
            Text(
                text = level.cefrCode,
                fontSize = 13.sp,
                color = TextGray
            )
        }
    }
}
