package com.androtext.core.render.theme

import androidx.compose.ui.graphics.Color

data class EditorTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val assetPath: String,
    val previewColors: List<String>,
)

data class ParsedThemeColors(
    val editorBackground: String,
    val editorForeground: String,
    val editorLineHighlightBackground: String?,
    val editorLineNumberForeground: String?,
    val editorLineNumberActiveForeground: String?,
    val editorSelectionBackground: String?,
    val editorSelectionForeground: String?,
    val editorCursorForeground: String?,
    val editorIndentGuideBackground: String?,
    val editorIndentGuideActiveBackground: String?,
    val editorOverviewRulerBorder: String?,
    val sideBarBackground: String?,
    val sideBarForeground: String?,
    val activityBarBackground: String?,
    val editorGroupHeaderBackground: String?,
    val statusBarBackground: String?,
    val statusBarForeground: String?,
    val tokenColors: List<TokenColorRule>,
)

data class TokenColorRule(
    val name: String?,
    val scope: List<String>,
    val settings: Map<String, String>,
)

data class ComposeThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    val surfaceTint: Color,
)
