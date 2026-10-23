package com.linguaflow.app.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.ui.theme.*

enum class PracticeSkillMode {
    SPEAKING,
    WRITING,
    LISTENING,
    MIX
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeHubScreen(
    languageName: String,
    flagEmoji: String,
    currentCefr: String,
    onStartPractice: (PracticeSkillMode) -> Unit,
    onBack: () -> Unit
) {
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
                description = "Usa tu voz con el micrófono para pronunciar frases en inglés y recibir evaluación de dicción al instante.",
                accentColor = AccentTeal,
                buttonText = "Comenzar Speaking",
                onClick = { onStartPractice(PracticeSkillMode.SPEAKING) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. WRITING
            SkillPracticeCard(
                emoji = "✍️",
                title = "Writing (Escritura)",
                description = "Fortalece tu ortografía, gramática y construcción de oraciones escribiendo directamente en inglés.",
                accentColor = PrimaryCoral,
                buttonText = "Comenzar Writing",
                onClick = { onStartPractice(PracticeSkillMode.WRITING) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. LISTENING
            SkillPracticeCard(
                emoji = "🎧",
                title = "Listening (Comprensión Auditiva)",
                description = "Entrena tu oído con pronunciaciones nativas a velocidad normal y lenta (tortuga 🐢).",
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
