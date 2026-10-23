package com.linguaflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IndigoCoralColorScheme = darkColorScheme(
    primary = PrimaryCoral,
    onPrimary = Color.White,
    primaryContainer = PrimaryCoralDark,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = IndigoBackground,
    error = ErrorRed,
    onError = Color.White,
    background = IndigoBackground,
    onBackground = TextLight,
    surface = IndigoCard,
    onSurface = TextLight,
    outline = CardBorder
)

@Composable
fun LinguaFlowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndigoCoralColorScheme,
        content = content
    )
}

