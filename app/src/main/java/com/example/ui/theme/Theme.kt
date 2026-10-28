package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TaskFlowDarkScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color(0xFF00194B),
    primaryContainer = Color(0xFF22357A),
    onPrimaryContainer = Color(0xFFDCE1FF),
    secondary = MintSecondary,
    onSecondary = Color(0xFF00382E),
    secondaryContainer = Color(0xFF005144),
    onSecondaryContainer = Color(0xFF70F8DD),
    tertiary = CoralTertiary,
    onTertiary = Color(0xFF5A1A06),
    tertiaryContainer = Color(0xFF7F280D),
    onTertiaryContainer = Color(0xFFFFDBD1),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF1E2436),
    error = PriorityUrgent,
    onError = Color.White
)

val TaskFlowLightScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E6FF),
    onPrimaryContainer = Color(0xFF081C61),
    secondary = Color(0xFF008975),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8F3E7),
    onSecondaryContainer = Color(0xFF00201A),
    tertiary = Color(0xFFC84521),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBD1),
    onTertiaryContainer = Color(0xFF3B0B00),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder,
    outlineVariant = Color(0xFFE2E7F0),
    error = PriorityUrgent,
    onError = Color.White
)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TaskFlowDarkScheme else TaskFlowLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
