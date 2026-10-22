package com.linguaflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = AccentYellow,
    error = ErrorRed,
    background = NeutralBackground,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = TextDark
)

@Composable
fun LinguaFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
