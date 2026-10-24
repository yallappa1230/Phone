package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IosGreen,
    onPrimary = Color.White,
    primaryContainer = IosDarkSurface,
    onPrimaryContainer = Color.White,
    secondary = IosBlue,
    onSecondary = Color.White,
    tertiary = IosOrange,
    background = IosDarkBackground,
    onBackground = IosDarkTextPrimary,
    surface = IosDarkSurface,
    onSurface = IosDarkTextPrimary,
    surfaceVariant = IosDarkKeypad,
    onSurfaceVariant = IosDarkTextSecondary,
    outline = IosDarkSeparator
)

private val LightColorScheme = lightColorScheme(
    primary = IosGreen,
    onPrimary = Color.White,
    primaryContainer = IosLightSurface,
    onPrimaryContainer = Color.Black,
    secondary = IosBlue,
    onSecondary = Color.White,
    tertiary = IosOrange,
    background = IosLightBackground,
    onBackground = IosLightTextPrimary,
    surface = IosLightSurface,
    onSurface = IosLightTextPrimary,
    surfaceVariant = IosLightKeypad,
    onSurfaceVariant = IosLightTextSecondary,
    outline = IosLightSeparator
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
