package com.rialtracker.expense.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PastelSkyDark,
    onPrimary = Color.White,
    primaryContainer = PastelSky,
    onPrimaryContainer = TextPrimary,
    secondary = PastelPeachDark,
    onSecondary = Color.White,
    secondaryContainer = PastelPeach,
    onSecondaryContainer = TextPrimary,
    tertiary = PastelPinkDark,
    tertiaryContainer = PastelPink,
    background = BackgroundWarm,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = PastelGray,
    onSurfaceVariant = TextSecondary,
    error = ErrorSoft,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = PastelSky,
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = PastelSkyDark,
    secondary = PastelPeach,
    background = Color(0xFF1E1E1E),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF2A2A2A),
    onSurface = Color(0xFFEDEDED),
    error = ErrorSoft
)

@Composable
fun RialTrackerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
