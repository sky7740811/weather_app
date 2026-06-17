package com.meteo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = Accent2,
    background = Bg,
    surface = Card,
    surfaceVariant = CardBorder,
    onPrimary = Text,
    onSecondary = Text,
    onBackground = Text,
    onSurface = Text,
    error = Danger,
)

@Composable
fun MeteoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
