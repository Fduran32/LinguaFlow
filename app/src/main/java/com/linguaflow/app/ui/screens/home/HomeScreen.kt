package com.linguaflow.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.domain.model.Language
import com.linguaflow.app.domain.model.User
import com.linguaflow.app.ui.components.UserAvatar
import com.linguaflow.app.ui.theme.*
import com.linguaflow.app.util.DailyGoalAndStreakManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: User,
    currentLanguageName: String,
    currentFlagEmoji: String,
    currentAssignedCefr: String,
    availableLanguages: List<Language>,
    pendingMistakesCount: Int,
    onSelectLanguage: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMistakesReview: () -> Unit,
    onOpenLearningPath: () -> Unit,
    onOpenPracticeHub: () -> Unit
) {
    val context = LocalContext.current
    val dailyStats = remember(currentUser.id) {
        DailyGoalAndStreakManager.getStats(context, currentUser.id)
    }
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
                        Text(text = currentLanguageName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (availableLanguages.isNotEmpty()) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Cambiar idioma")
                        }
                    }

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
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenMistakesReview)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (pendingMistakesCount > 0) {
                                    Badge(
                                        containerColor = ErrorRed,
                                        contentColor = Color.White,
                                        modifier = Modifier.offset(x = (-2).dp, y = 2.dp)
                                    ) {
                                        Text(
                                            text = "$pendingMistakesCount",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "Cuaderno de Errores",
                                tint = if (pendingMistakesCount > 0) ErrorRed else TextDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(onClick = onOpenProfile) {
                        UserAvatar(
                            avatar = currentUser.avatar,
                            isCustomPhoto = currentUser.isCustomPhoto,
                            size = 36.dp,
                            textSize = 18.sp,
                            borderWidth = 1.5.dp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoBackground,
                    titleContentColor = TextLight,
                    actionIconContentColor = TextLight
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
            // Saludo y tarjeta de nivel del usuario
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        avatar = currentUser.avatar,
                        isCustomPhoto = currentUser.isCustomPhoto,
                        size = 54.dp,
                        textSize = 28.sp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Hola, ${currentUser.username}!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Nivel actual en $currentLanguageName:",
                                fontSize = 12.sp,
                                color = TextMedium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = currentAssignedCefr,
                                    color = PrimaryGreenDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Opción 2 (Fase 3): Tarjeta de Racha Real y Meta Diaria de Estudio
            val progressRatio = (dailyStats.completedToday.toFloat() / dailyStats.dailyTarget.toFloat()).coerceIn(0f, 1f)
            val isGoalReached = dailyStats.completedToday >= dailyStats.dailyTarget
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isGoalReached) PrimaryGreen.copy(alpha = 0.5f) else CardBorder
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎯", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isGoalReached) "¡Meta Diaria Cumplida! 🎉" else "Meta Diaria de Estudio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "${dailyStats.completedToday} de ${dailyStats.dailyTarget} sesiones completadas hoy",
                                    fontSize = 12.sp,
                                    color = TextMedium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentYellow.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔥", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${dailyStats.streakDays} día${if (dailyStats.streakDays == 1) "" else "s"}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isGoalReached) PrimaryGreen else PrimaryCoral,
                        trackColor = IndigoSurfaceLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¿Qué deseas practicar hoy?",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // CARD 1: RUTA DE APRENDIZAJE (MCER A1 - C2)
            // ========================================================
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenLearningPath() }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryCoral, PrimaryCoralDark)
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🗺️", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Ruta de Aprendizaje",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Lecciones y evaluaciones del MCER",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Avanza por tus 10 subniveles progresivos de A1 a C2. Supera los exámenes finales para desbloquear el siguiente nivel.",
                            fontSize = 14.sp,
                            color = TextLight,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onOpenLearningPath,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Entrar a la Ruta de Niveles", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // CARD 2: CENTRO DE PRÁCTICA POR HABILIDADES
            // ========================================================
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPracticeHub() }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF384B70), Color(0xFF1E2640))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎯", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Centro de Entrenamiento",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Práctica libre adaptada a tu nivel $currentAssignedCefr",
                                    fontSize = 13.sp,
                                    color = AccentTeal
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Entrena habilidades específicas a tu propio ritmo con ejercicios de pronunciación con micrófono, redacción y comprensión auditiva:",
                            fontSize = 14.sp,
                            color = TextLight,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Badges de Habilidades
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SkillBadge("🗣️ Speaking", modifier = Modifier.weight(1f))
                            SkillBadge("✍️ Writing", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SkillBadge("🎧 Listening", modifier = Modifier.weight(1f))
                            SkillBadge("🔀 Mix Completo", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onOpenPracticeHub,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abrir Centro de Práctica", fontWeight = FontWeight.Bold, color = IndigoBackground)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = IndigoBackground, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SkillBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = IndigoSurfaceLight,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextLight,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
        )
    }
}
