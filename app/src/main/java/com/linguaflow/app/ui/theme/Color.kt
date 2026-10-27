package com.linguaflow.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Estado reactivo del tema que se sincroniza automáticamente con el Modo Oscuro / Modo Claro del teléfono.
 */
object AppThemeState {
    var isDark by mutableStateOf(true)
}

// =====================================================================
// Paleta Dual: Deep Indigo & Warm Coral (Modo Oscuro y Modo Claro Automático)
// =====================================================================
val PrimaryCoral = Color(0xFFFF6B4A)          // Coral cálido / Mandarina vibrante
val PrimaryCoralDark = Color(0xFFE04826)      // Coral profundo para interacción

val IndigoBackground: Color
    get() = if (AppThemeState.isDark) Color(0xFF121829) else Color(0xFFF8FAFC)

val IndigoCard: Color
    get() = if (AppThemeState.isDark) Color(0xFF1E2640) else Color(0xFFFFFFFF)

val IndigoSurfaceLight: Color
    get() = if (AppThemeState.isDark) Color(0xFF263252) else Color(0xFFF1F5F9)

val AccentTeal: Color
    get() = if (AppThemeState.isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)

val AccentYellow: Color
    get() = if (AppThemeState.isDark) Color(0xFFFFC800) else Color(0xFFF59E0B)

val ErrorRed = Color(0xFFEF4444)
val ErrorRedLight: Color
    get() = if (AppThemeState.isDark) Color(0x33EF4444) else Color(0xFFFEF2F2)

val SuccessGreen = Color(0xFF10B981)
val SuccessGreenLight: Color
    get() = if (AppThemeState.isDark) Color(0x3310B981) else Color(0xFFECFDF5)

val CardBorder: Color
    get() = if (AppThemeState.isDark) Color(0xFF2E3B5B) else Color(0xFFE2E8F0)

val TextLight: Color
    get() = if (AppThemeState.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

val TextMuted: Color
    get() = if (AppThemeState.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

val LockedGray: Color
    get() = if (AppThemeState.isDark) Color(0xFF475569) else Color(0xFF94A3B8)

// Aliases reactivos para compatibilidad en todas las pantallas
val PrimaryGreen: Color
    get() = PrimaryCoral

val PrimaryGreenDark: Color
    get() = PrimaryCoralDark

val NeutralBackground: Color
    get() = IndigoBackground

val TextDark: Color
    get() = TextLight

val TextGray: Color
    get() = TextMuted

val TextMedium: Color
    get() = TextMuted
