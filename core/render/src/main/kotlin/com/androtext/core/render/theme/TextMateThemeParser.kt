package com.androtext.core.render.theme

import android.graphics.Color
import androidx.compose.ui.graphics.Color as ComposeColor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.json.JSONObject

object TextMateThemeParser {

    fun parseEditorColorScheme(json: String): EditorColorScheme {
        val root = JSONObject(json)
        val colors = root.optJSONObject("colors") ?: JSONObject()
        val tokenColors = parseTokenColors(root)

        val scheme = EditorColorScheme()

        val bg = colors.optString("editor.background", "#002B36")
        val fg = colors.optString("editor.foreground", "#839496")
        val lineHighlight = colors.optString("editor.lineHighlightBackground", adjustBrightness(bg, 0.15f))
        val lineNumberFg = colors.optString("editorLineNumber.foreground", adjustAlpha(fg, 0.5f))
        val lineNumberActiveFg = colors.optString("editorLineNumber.activeForeground", fg)
        val selectionBg = colors.optString("editor.selectionBackground", adjustAlpha(accentFromTokens(tokenColors), 0.4f))
        val selectionFg = colors.optString("editor.selectionForeground", bg)
        val cursorFg = colors.optString("editorCursor.foreground", fg)
        val indentGuide = colors.optString("editorIndentGuide.background", adjustAlpha(fg, 0.2f))
        val indentGuideActive = colors.optString("editorIndentGuide.activeBackground", adjustAlpha(fg, 0.4f))
        val scrollThumb = adjustAlpha(fg, 0.3f)
        val scrollTrack = adjustAlpha(fg, 0.1f)
        val lineDivider = colors.optString("editorGroupHeader.tabsBackground", adjustAlpha(bg, 0.0f))

        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, parseColor(bg))
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, parseColor(fg))
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, parseColor(adjustBrightness(bg, 0.08f)))
        scheme.setColor(EditorColorScheme.LINE_NUMBER, parseColor(lineNumberFg))
        scheme.setColor(EditorColorScheme.LINE_NUMBER_CURRENT, parseColor(lineNumberActiveFg))
        scheme.setColor(EditorColorScheme.CURRENT_LINE, parseColor(lineHighlight))
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, parseColor(cursorFg))
        scheme.setColor(EditorColorScheme.SELECTION_HANDLE, parseColor(selectionBg))
        scheme.setColor(EditorColorScheme.SELECTED_TEXT_BACKGROUND, parseColor(selectionBg))
        scheme.setColor(EditorColorScheme.TEXT_SELECTED, parseColor(selectionFg))
        scheme.setColor(EditorColorScheme.BLOCK_LINE, parseColor(indentGuide))
        scheme.setColor(EditorColorScheme.BLOCK_LINE_CURRENT, parseColor(indentGuideActive))
        scheme.setColor(EditorColorScheme.SCROLL_BAR_THUMB, parseColor(scrollThumb))
        scheme.setColor(EditorColorScheme.SCROLL_BAR_TRACK, parseColor(scrollTrack))
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, parseColor(lineDivider))
        scheme.setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_BACKGROUND, parseColor(adjustAlpha(fg, 0.15f)))

        val keywordColor = findTokenColor(tokenColors, listOf("keyword", "keyword.control", "storage", "storage.type"))
            ?: fg
        val commentColor = findTokenColor(tokenColors, listOf("comment"))
            ?: adjustAlpha(fg, 0.5f)
        val stringColor = findTokenColor(tokenColors, listOf("string"))
            ?: fg
        val numberColor = findTokenColor(tokenColors, listOf("constant.numeric", "constant.language"))
            ?: fg
        val functionColor = findTokenColor(tokenColors, listOf("entity.name.function", "support.function"))
            ?: fg
        val annotationColor = findTokenColor(tokenColors, listOf("meta.decorator", "storage.type.annotation"))
            ?: fg
        val operatorColor = findTokenColor(tokenColors, listOf("keyword.operator"))
            ?: fg
        val variableColor = findTokenColor(tokenColors, listOf("variable"))
            ?: fg

        scheme.setColor(EditorColorScheme.KEYWORD, parseColor(keywordColor))
        scheme.setColor(EditorColorScheme.COMMENT, parseColor(commentColor))
        scheme.setColor(EditorColorScheme.LITERAL, parseColor(stringColor))
        scheme.setColor(EditorColorScheme.FUNCTION_NAME, parseColor(functionColor))
        scheme.setColor(EditorColorScheme.ANNOTATION, parseColor(annotationColor))
        scheme.setColor(EditorColorScheme.OPERATOR, parseColor(operatorColor))

        return scheme
    }

    fun parseComposeColors(json: String, isDark: Boolean): ComposeThemeColors {
        val root = JSONObject(json)
        val colors = root.optJSONObject("colors") ?: JSONObject()
        val tokenColors = parseTokenColors(root)

        val bg = parseComposeColor(colors.optString("editor.background", if (isDark) "#002B36" else "#FFFFFF"))
        val fg = parseComposeColor(colors.optString("editor.foreground", if (isDark) "#839496" else "#333333"))

        val primary = parseComposeColor(
            findTokenColor(tokenColors, listOf("entity.name.function", "support.function"))
                ?: colors.optString("editor.selectionBackground", "#268BD2")
        )
        val keywordHex = findTokenColor(tokenColors, listOf("keyword", "keyword.control", "storage", "storage.type"))
        val secondary = parseComposeColor(
            keywordHex ?: adjustAlpha(toHex(primary), 0.7f)
        )
        val stringHex = findTokenColor(tokenColors, listOf("string"))
        val tertiary = parseComposeColor(
            stringHex ?: "#859900"
        )

        val surfaceVariant = adjustComposeBrightness(bg, if (isDark) 0.08f else -0.04f)
        val onSurfaceVariant = adjustComposeBrightness(fg, if (isDark) -0.3f else -0.4f)
        val outline = adjustComposeBrightness(fg, if (isDark) -0.4f else -0.55f)
        val outlineVariant = adjustComposeBrightness(fg, if (isDark) -0.55f else -0.7f)
        val primaryContainer = adjustComposeBrightness(primary, if (isDark) -0.3f else 0.6f)
        val onPrimaryContainer = if (isDark) fg else bg
        val onPrimary = if (isDark) bg else fg
        val secondaryContainer = adjustComposeBrightness(secondary, if (isDark) -0.3f else 0.6f)
        val onSecondaryContainer = if (isDark) fg else bg
        val onSecondary = if (isDark) bg else fg
        val tertiaryContainer = adjustComposeBrightness(tertiary, if (isDark) -0.3f else 0.6f)
        val onTertiary = if (isDark) bg else fg
        val onTertiaryContainer = if (isDark) fg else bg
        val error = parseComposeColor(if (isDark) "#DC322F" else "#CF222E")
        val onError = parseComposeColor(if (isDark) "#002B36" else "#FFFFFF")
        val errorContainer = adjustComposeBrightness(error, if (isDark) -0.3f else 0.6f)
        val onErrorContainer = parseComposeColor(if (isDark) "#DC322F" else "#CF222E")
        val inverseSurface = fg
        val inverseOnSurface = bg
        val inversePrimary = adjustComposeBrightness(primary, if (isDark) 0.4f else -0.4f)
        val surfaceTint = primary

        return ComposeThemeColors(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = bg,
            onBackground = fg,
            surface = bg,
            onSurface = fg,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            surfaceTint = surfaceTint,
        )
    }

    private fun parseTokenColors(root: JSONObject): List<TokenColorRule> {
        val arr = root.optJSONArray("tokenColors") ?: return emptyList()
        val rules = mutableListOf<TokenColorRule>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val scope = obj.opt("scope")
            val scopeList = when (scope) {
                is String -> listOf(scope)
                is org.json.JSONArray -> (0 until scope.length()).map { scope.getString(it) }
                else -> emptyList()
            }
            val settings = obj.optJSONObject("settings") ?: continue
            val settingsMap = mutableMapOf<String, String>()
            for (key in settings.keys()) {
                val value = settings.optString(key, "")
                if (value.isNotEmpty()) settingsMap[key] = value
            }
            rules.add(TokenColorRule(
                name = obj.optString("name", ""),
                scope = scopeList,
                settings = settingsMap,
            ))
        }
        return rules
    }

    private fun findTokenColor(rules: List<TokenColorRule>, targetScopes: List<String>): String? {
        for (rule in rules) {
            for (scope in rule.scope) {
                if (scope in targetScopes) {
                    return rule.settings["foreground"]
                }
            }
        }
        return null
    }

    private fun accentFromTokens(rules: List<TokenColorRule>): String {
        return findTokenColor(rules, listOf("entity.name.function", "support.function"))
            ?: findTokenColor(rules, listOf("keyword", "keyword.control"))
            ?: "#268BD2"
    }

    private fun parseColor(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (_: Exception) {
            Color.parseColor("#839496")
        }
    }

    private fun parseComposeColor(hex: String): ComposeColor {
        return try {
            ComposeColor(Color.parseColor(hex))
        } catch (_: Exception) {
            ComposeColor(0xFF839496.toInt())
        }
    }

    private fun adjustBrightness(hex: String, amount: Float): String {
        val color = parseColor(hex)
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        val nr = (r + amount).coerceIn(0f, 1f)
        val ng = (g + amount).coerceIn(0f, 1f)
        val nb = (b + amount).coerceIn(0f, 1f)
        return String.format("#%02X%02X%02X", (nr * 255).toInt(), (ng * 255).toInt(), (nb * 255).toInt())
    }

    private fun adjustAlpha(hex: String, alpha: Float): String {
        val color = parseColor(hex)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        return String.format("#%02X%02X%02X%02X", a, r, g, b)
    }

    private fun adjustComposeBrightness(color: ComposeColor, amount: Float): ComposeColor {
        val r = (color.red + amount).coerceIn(0f, 1f)
        val g = (color.green + amount).coerceIn(0f, 1f)
        val b = (color.blue + amount).coerceIn(0f, 1f)
        return ComposeColor(r, g, b, color.alpha)
    }

    private fun toHex(color: ComposeColor): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
