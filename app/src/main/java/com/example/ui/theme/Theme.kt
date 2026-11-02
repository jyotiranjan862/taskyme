package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.data.repository.AppColorTheme

fun getTaskFlowColorScheme(
    darkTheme: Boolean,
    colorTheme: AppColorTheme
): ColorScheme {
    val primaryColor = Color(if (darkTheme) colorTheme.primaryDarkHex else colorTheme.primaryHex)
    val secondaryColor = Color(colorTheme.secondaryHex)

    return if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color(0xFF060914),
            primaryContainer = primaryColor.copy(alpha = 0.22f),
            onPrimaryContainer = Color(0xFFE2E8F0),
            secondary = secondaryColor,
            onSecondary = Color(0xFF021B14),
            secondaryContainer = secondaryColor.copy(alpha = 0.2f),
            onSecondaryContainer = Color(0xFFD1FAE5),
            tertiary = CoralTertiary,
            onTertiary = Color(0xFF431407),
            tertiaryContainer = Color(0xFF7C2D12),
            onTertiaryContainer = Color(0xFFFFEDD5),
            background = DarkBackground,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceElevated,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkCardBorder,
            outlineVariant = Color(0xFF1E283E),
            error = PriorityUrgent,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.12f),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = secondaryColor.copy(alpha = 0.12f),
            onSecondaryContainer = Color(0xFF065F46),
            tertiary = CoralTertiary,
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFEDD5),
            onTertiaryContainer = Color(0xFF9A3412),
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceElevated,
            onSurfaceVariant = LightTextSecondary,
            outline = LightCardBorder,
            outlineVariant = Color(0xFFE2E8F0),
            error = PriorityUrgent,
            onError = Color.White
        )
    }
}

val TaskFlowDarkScheme = getTaskFlowColorScheme(true, AppColorTheme.INDIGO)
val TaskFlowLightScheme = getTaskFlowColorScheme(false, AppColorTheme.INDIGO)

@Composable
fun TaskFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: AppColorTheme = AppColorTheme.INDIGO,
    content: @Composable () -> Unit
) {
    val colorScheme = remember(darkTheme, colorTheme) {
        getTaskFlowColorScheme(darkTheme, colorTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

