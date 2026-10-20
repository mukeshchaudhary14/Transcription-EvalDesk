package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = CyanDark,
    primaryContainer = Slate800,
    onPrimaryContainer = ElectricCyan,
    secondary = SkyBlue,
    onSecondary = Color.Black,
    secondaryContainer = Slate800,
    onSecondaryContainer = SkyBlue,
    tertiary = EmeraldPass,
    onTertiary = Color.Black,
    background = Slate950,
    onBackground = Slate200,
    surface = Slate900,
    onSurface = Slate200,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    error = CrimsonAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Slate100,
    onPrimaryContainer = CyanPrimaryLight,
    secondary = SkyBlue,
    onSecondary = Color.Black,
    tertiary = EmeraldPass,
    background = Slate50,
    onBackground = Slate950,
    surface = Color.White,
    onSurface = Slate950,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate400,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional engineer dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // High-density engineer palette default

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
