package com.androtext.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SolarizedDark = darkColorScheme(
    primary = Color(0xFF268BD2),
    onPrimary = Color(0xFF002B36),
    primaryContainer = Color(0xFF073642),
    onPrimaryContainer = Color(0xFF93A1A1),
    secondary = Color(0xFF2AA198),
    onSecondary = Color(0xFF002B36),
    secondaryContainer = Color(0xFF073642),
    onSecondaryContainer = Color(0xFF93A1A1),
    tertiary = Color(0xFF859900),
    onTertiary = Color(0xFF002B36),
    background = Color(0xFF002B36),
    onBackground = Color(0xFF839496),
    surface = Color(0xFF002B36),
    onSurface = Color(0xFF839496),
    surfaceVariant = Color(0xFF073642),
    onSurfaceVariant = Color(0xFF586E75),
    outline = Color(0xFF586E75),
    error = Color(0xFFDC322F),
    onError = Color(0xFF002B36),
    errorContainer = Color(0xFF073642),
    onErrorContainer = Color(0xFFDC322F),
)

@Composable
fun AndroTextTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SolarizedDark,
        content = content,
    )
}
