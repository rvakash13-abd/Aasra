package com.example.aasra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AasraGreen,
    onPrimary = AasraOnPrimary,
    primaryContainer = AasraGreenLight,
    onPrimaryContainer = Color.White,
    secondary = AasraAmber,
    onSecondary = Color.Black,
    background = AasraBackground,
    onBackground = Color(0xFF1B1B1B),
    surface = AasraSurface,
    onSurface = Color(0xFF1B1B1B),
    error = AasraDanger,
    onError = Color.White,
    tertiary = AasraAmber
)

@Composable
fun AasraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AasraTypography,
        content = content
    )
}