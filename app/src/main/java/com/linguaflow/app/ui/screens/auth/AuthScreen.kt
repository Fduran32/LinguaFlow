package com.linguaflow.app.ui.screens.auth

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linguaflow.app.domain.model.AuthResult
import com.linguaflow.app.domain.util.AuthValidator
import com.linguaflow.app.ui.components.UserAvatar
import com.linguaflow.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onRegister: suspend (String, String, String, Boolean) -> AuthResult,
    onLogin: suspend (String, String) -> AuthResult
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    // Campos Login
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Campos Registro
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regConfirmPasswordVisible by remember { mutableStateOf(false) }
    var regSelectedAvatar by remember { mutableStateOf("🦉") }
    var regIsCustomPhoto by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    val defaultAvatars = listOf("🦉", "🦊", "🦁", "🐼", "🚀", "🎓", "🐱", "🎨")
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Selector de galería para avatar personalizado
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToAppStorage(context, uri)
            if (savedPath != null) {
                regSelectedAvatar = savedPath
                regIsCustomPhoto = true
            }
        }
    }

    val passwordCriteria = remember(regPassword) {
        AuthValidator.checkPasswordCriteria(regPassword)
    }

    Scaffold(
        containerColor = Color(0xFFF7F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo & Header
            Text(
                text = "🌱 LinguaFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryGreenDark
            )
            Text(
                text = "Aprende idiomas con fluidez y constancia",
                fontSize = 14.sp,
                color = TextMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Selector Tabs: Iniciar Sesión / Registrarse
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE5E7EB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Button(
                        onClick = {
                            isRegisterMode = false
                            loginError = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isRegisterMode) Color.White else Color.Transparent,
                            contentColor = if (!isRegisterMode) PrimaryGreenDark else TextMedium
                        ),
                        elevation = if (!isRegisterMode) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Iniciar Sesión", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isRegisterMode = true
                            regError = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegisterMode) Color.White else Color.Transparent,
                            contentColor = if (isRegisterMode) PrimaryGreenDark else TextMedium
                        ),
                        elevation = if (isRegisterMode) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Registrarse", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isRegisterMode) {
                // ==========================================
                // FORMULARIO: INICIAR SESIÓN
                // ==========================================
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
                            text = "¡Hola de nuevo!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Ingresa con tu usuario para continuar aprendiendo",
                            fontSize = 13.sp,
                            color = TextMedium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Usuario
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                loginError = null
                            },
                            label = { Text("Nombre de usuario") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Contraseña
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                loginError = null
                            },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen) },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (loginError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ErrorRed.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = loginError ?: "",
                                        color = ErrorRed,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (loginUsername.isBlank() || loginPassword.isBlank()) {
                                    loginError = "Por favor completa todos los campos."
                                    return@Button
                                }
                                isLoggingIn = true
                                coroutineScope.launch {
                                    val result = onLogin(loginUsername, loginPassword)
                                    isLoggingIn = false
                                    when (result) {
                                        is AuthResult.Success -> {
                                            onLoginSuccess()
                                        }
                                        is AuthResult.Error -> {
                                            loginError = result.message
                                        }
                                    }
                                }
                            },
                            enabled = !isLoggingIn,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // FORMULARIO: REGISTRO
                // ==========================================
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
                            text = "Crea tu Cuenta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Comienza desde A1 y sube de nivel con tu propio perfil",
                            fontSize = 13.sp,
                            color = TextMedium
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Avatar Preview y Selector
                        Text("Elige tu Avatar o Foto de Perfil:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserAvatar(
                                avatar = regSelectedAvatar,
                                isCustomPhoto = regIsCustomPhoto,
                                size = 64.dp,
                                textSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Subir Foto", fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fila de Emojis de Avatares predefinidos
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(defaultAvatars) { emoji ->
                                val isSelected = !regIsCustomPhoto && regSelectedAvatar == emoji
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else Color(0xFFF3F4F6))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) PrimaryGreen else Color(0xFFD1D5DB),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            regSelectedAvatar = emoji
                                            regIsCustomPhoto = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Nombre de Usuario
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = {
                                regUsername = it
                                regError = null
                            },
                            label = { Text("Nombre de usuario") },
                            placeholder = { Text("Mínimo 3 caracteres") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Contraseña
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = {
                                regPassword = it
                                regError = null
                            },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        if (regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Criterios de Validación de Contraseña en vivo
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Requisitos de seguridad:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            PasswordCriterionRow("Mínimo 6 caracteres", passwordCriteria.hasMinLength)
                            PasswordCriterionRow("Al menos una mayúscula (A-Z)", passwordCriteria.hasUpperCase)
                            PasswordCriterionRow("Letras minúsculas (a-z)", passwordCriteria.hasLowerCase)
                            PasswordCriterionRow("Carácter especial (@, #, $, %, etc.)", passwordCriteria.hasSpecialChar)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirmar Contraseña
                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = {
                                regConfirmPassword = it
                                regError = null
                            },
                            label = { Text("Confirmar contraseña") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = PrimaryGreen) },
                            trailingIcon = {
                                IconButton(onClick = { regConfirmPasswordVisible = !regConfirmPasswordVisible }) {
                                    Icon(
                                        if (regConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (regConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (regError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ErrorRed.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = regError ?: "",
                                        color = ErrorRed,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón Registrar
                        Button(
                            onClick = {
                                val userErr = AuthValidator.validateUsername(regUsername)
                                if (userErr != null) {
                                    regError = userErr
                                    return@Button
                                }
                                val passErr = AuthValidator.validatePassword(regPassword)
                                if (passErr != null) {
                                    regError = passErr
                                    return@Button
                                }
                                if (regPassword != regConfirmPassword) {
                                    regError = "Las contraseñas no coinciden."
                                    return@Button
                                }

                                isRegistering = true
                                coroutineScope.launch {
                                    val result = onRegister(
                                        regUsername.trim(),
                                        regPassword,
                                        regSelectedAvatar,
                                        regIsCustomPhoto
                                    )
                                    isRegistering = false
                                    when (result) {
                                        is AuthResult.Success -> {
                                            onLoginSuccess()
                                        }
                                        is AuthResult.Error -> {
                                            regError = result.message
                                        }
                                    }
                                }
                            },
                            enabled = !isRegistering,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isRegistering) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text("Crear Cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tu progreso y nivel por idioma se guardan de forma segura e independiente.",
                fontSize = 12.sp,
                color = TextMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PasswordCriterionRow(label: String, isSatisfied: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isSatisfied) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSatisfied) PrimaryGreenDark else Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSatisfied) PrimaryGreenDark else Color(0xFF4B5563),
            fontWeight = if (isSatisfied) FontWeight.SemiBold else FontWeight.Normal
        )
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
