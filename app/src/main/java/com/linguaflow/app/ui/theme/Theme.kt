package com.linguaflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkIndigoCoralScheme = darkColorScheme(
    primary = PrimaryCoral,
    onPrimary = Color.White,
    primaryContainer = PrimaryCoralDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF121829),
    error = ErrorRed,
    onError = Color.White,
    background = Color(0xFF121829),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E2640),
    onSurface = Color(0xFFF8FAFC),
    outline = Color(0xFF2E3B5B)
)

private val LightIndigoCoralScheme = lightColorScheme(
    primary = PrimaryCoral,
    onPrimary = Color.White,
    primaryContainer = PrimaryCoralDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFFE2E8F0)
)

@Composable
fun LinguaFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AppThemeState.isDark = darkTheme
    val colorScheme = if (darkTheme) DarkIndigoCoralScheme else LightIndigoCoralScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
