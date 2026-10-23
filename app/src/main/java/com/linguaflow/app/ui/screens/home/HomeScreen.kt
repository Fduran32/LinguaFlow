package com.linguaflow.app.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.domain.model.Language
import com.linguaflow.app.domain.model.User
import com.linguaflow.app.ui.components.UserAvatar
import com.linguaflow.app.ui.theme.*

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F9FA)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
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

            Spacer(modifier = Modifier.height(24.dp))

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
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    colors = listOf(PrimaryGreen, PrimaryGreenDark)
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
                            color = TextDark,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onOpenLearningPath,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Entrar a la Ruta de Niveles", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
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
                            text = "Entrena habilidades específicas a tu propio ritmo con ejercicios de pronunciación con micrófono, redacción y comprensión auditiva:",
                            fontSize = 14.sp,
                            color = TextDark,
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Abrir Centro de Práctica", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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
        color = Color(0xFFF3F4F6),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151),
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
        )
    }
}
