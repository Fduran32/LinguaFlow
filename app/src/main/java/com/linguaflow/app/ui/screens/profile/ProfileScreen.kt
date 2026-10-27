package com.linguaflow.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
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
import com.linguaflow.app.domain.model.Language
import com.linguaflow.app.domain.model.User
import com.linguaflow.app.domain.util.AuthValidator
import com.linguaflow.app.ui.components.UserAvatar
import com.linguaflow.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    languages: List<Language>,
    languageLevels: Map<String, String>,
    completedLevelsCount: Int = 0,
    pendingMistakesCount: Int = 0,
    onUpdateProfile: (String, String, Boolean) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val formattedDate = remember(user.createdAt) {
        val sdf = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
        sdf.format(Date(user.createdAt))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = TextLight) },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta Principal del Usuario
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        avatar = user.avatar,
                        isCustomPhoto = user.isCustomPhoto,
                        size = 90.dp,
                        textSize = 46.sp,
                        borderWidth = 3.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = user.username,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = "Miembro desde el $formattedDate",
                        fontSize = 13.sp,
                        color = TextMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar Perfil", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Opción 3: Panel de Estadísticas Globales con Racha Real
            val context = LocalContext.current
            val dailyStats = remember(user.id) {
                com.linguaflow.app.util.DailyGoalAndStreakManager.getStats(context, user.id)
            }
            val totalGems = 120 + (completedLevelsCount * 25) + (dailyStats.completedToday * 15)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatBox(
                    emoji = "🔥",
                    value = "${dailyStats.streakDays} día${if (dailyStats.streakDays == 1) "" else "s"}",
                    label = "Racha Activa",
                    modifier = Modifier.weight(1f)
                )
                ProfileStatBox(
                    emoji = "💎",
                    value = "$totalGems",
                    label = "Gemas XP",
                    modifier = Modifier.weight(1f)
                )
                ProfileStatBox(
                    emoji = "✅",
                    value = "$completedLevelsCount",
                    label = "Subniveles",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Opción 3: Vitrina de Medallas y Logros Políglotas
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logros y Medallas Políglotas",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val hasAdvancedLevel = languageLevels.values.any { !it.equals("A1", ignoreCase = true) }
                    val achievements = listOf(
                        AchievementBadge(
                            emoji = "🎯",
                            title = "Primer Paso",
                            description = "Iniciar tu camino políglota en LinguaFlow",
                            isUnlocked = true
                        ),
                        AchievementBadge(
                            emoji = "📖",
                            title = "Estudiante Constante",
                            description = "Superar tu primer subnivel o test de ubicación",
                            isUnlocked = completedLevelsCount > 0 || hasAdvancedLevel
                        ),
                        AchievementBadge(
                            emoji = "🧠",
                            title = "Mente Implacable",
                            description = "Mantener 0 errores pendientes en el Repaso SRS",
                            isUnlocked = pendingMistakesCount == 0
                        ),
                        AchievementBadge(
                            emoji = "🌍",
                            title = "Espíritu Políglota",
                            description = "Explorar los 3 idiomas (Inglés, Italiano y Chino)",
                            isUnlocked = languages.size >= 3
                        )
                    )

                    achievements.forEach { badge ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (badge.isUnlocked) IndigoSurfaceLight else LockedGray.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (badge.isUnlocked) AccentTeal.copy(alpha = 0.45f) else CardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (badge.isUnlocked) AccentYellow.copy(alpha = 0.2f)
                                            else LockedGray.copy(alpha = 0.3f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = if (badge.isUnlocked) badge.emoji else "🔒", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = badge.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (badge.isUnlocked) TextDark else TextMedium
                                    )
                                    Text(
                                        text = badge.description,
                                        fontSize = 12.sp,
                                        color = TextMedium
                                    )
                                }
                                if (badge.isUnlocked) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PrimaryGreen.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "Logrado",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGreenDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de Niveles Asignados por Idioma
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = PrimaryGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mis Niveles por Idioma",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (languages.isEmpty()) {
                        Text("Cargando información de idiomas...", color = TextMedium, fontSize = 13.sp)
                    } else {
                        languages.forEach { lang ->
                            val levelCefr = languageLevels[lang.id] ?: "A1"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IndigoSurfaceLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = lang.flagEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lang.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Marco Común Europeo (MCER)",
                                            fontSize = 12.sp,
                                            color = TextMedium
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PrimaryGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = levelCefr,
                                            color = PrimaryGreenDark,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Cerrar Sesión
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }

    // Cuadro de Diálogo: Editar Perfil
    if (showEditDialog) {
        EditProfileDialog(
            currentUser = user,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newAvatar, isPhoto ->
                onUpdateProfile(newName, newAvatar, isPhoto)
                showEditDialog = false
            }
        )
    }

    // Diálogo de Confirmación: Cerrar Sesión
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = IndigoCard,
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text("¿Deseas cerrar tu sesión actual? Tu progreso quedará guardado para cuando vuelvas a ingresar.", color = TextMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Cerrar Sesión", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancelar", color = TextMedium)
                }
            }
        )
    }
}

@Composable
private fun EditProfileDialog(
    currentUser: User,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var editName by remember { mutableStateOf(currentUser.username) }
    var editAvatar by remember { mutableStateOf(currentUser.avatar) }
    var editIsCustomPhoto by remember { mutableStateOf(currentUser.isCustomPhoto) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val defaultAvatars = listOf("🦉", "🦊", "🦁", "🐼", "🚀", "🎓", "🐱", "🎨")
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToAppStorage(context, uri)
            if (savedPath != null) {
                editAvatar = savedPath
                editIsCustomPhoto = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IndigoCard,
        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, color = TextDark) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    avatar = editAvatar,
                    isCustomPhoto = editIsCustomPhoto,
                    size = 72.dp,
                    textSize = 36.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cambiar Foto de Galería", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(defaultAvatars) { emoji ->
                        val isSelected = !editIsCustomPhoto && editAvatar == emoji
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else IndigoSurfaceLight)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PrimaryGreen else CardBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    editAvatar = emoji
                                    editIsCustomPhoto = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editName,
                    onValueChange = {
                        editName = it
                        errorMessage = null
                    },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = ErrorRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validErr = AuthValidator.validateUsername(editName)
                    if (validErr != null) {
                        errorMessage = validErr
                        return@Button
                    }
                    onSave(editName.trim(), editAvatar, editIsCustomPhoto)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Guardar Cambios", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMedium)
            }
        }
    )
}

private data class AchievementBadge(
    val emoji: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

@Composable
private fun ProfileStatBox(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = TextDark
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMedium
            )
        }
    }
}

private fun saveImageToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val avatarDir = File(context.filesDir, "avatars")
        if (!avatarDir.exists()) {
            avatarDir.mkdirs()
        }
        val targetFile = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        targetFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
