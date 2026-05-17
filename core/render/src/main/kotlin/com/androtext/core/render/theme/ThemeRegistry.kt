package com.androtext.core.render.theme

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.json.JSONArray

class ThemeRegistry private constructor() {

    private val themes = mutableListOf<EditorTheme>()
    private val editorSchemeCache = mutableMapOf<String, EditorColorScheme>()
    private val composeColorsCache = mutableMapOf<String, ComposeThemeColors>()
    private var currentThemeId: String = DEFAULT_THEME_ID
    private var onThemeChangedListener: ((String) -> Unit)? = null

    fun loadThemesFromAssets(assetManager: AssetManager) {
        themes.clear()
        editorSchemeCache.clear()
        composeColorsCache.clear()
        try {
            val indexJson = assetManager.open(THEME_INDEX_PATH).bufferedReader().use { it.readText() }
            val arr = JSONArray(indexJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                val previewArr = obj.getJSONArray("previewColors")
                val previewColors = (0 until previewArr.length()).map { previewArr.getString(it) }
                themes.add(EditorTheme(
                    id = id,
                    name = obj.getString("name"),
                    isDark = obj.getBoolean("isDark"),
                    assetPath = "$THEMES_DIR/${obj.getString("fileName")}",
                    previewColors = previewColors,
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load theme index", e)
        }
    }

    fun getAvailableThemes(): List<EditorTheme> = themes.toList()

    fun getTheme(id: String): EditorTheme? = themes.find { it.id == id }

    fun getCurrentTheme(): EditorTheme = getTheme(currentThemeId) ?: themes.first()

    fun getCurrentThemeId(): String = currentThemeId

    fun setCurrentThemeId(id: String) {
        if (themes.any { it.id == id }) {
            currentThemeId = id
            editorSchemeCache.remove(id)
            composeColorsCache.remove(id)
            onThemeChangedListener?.invoke(id)
        }
    }

    fun setOnThemeChangedListener(listener: ((String) -> Unit)?) {
        onThemeChangedListener = listener
    }

    fun getEditorColorScheme(assetManager: AssetManager, themeId: String): EditorColorScheme? {
        editorSchemeCache[themeId]?.let { return it }
        val theme = getTheme(themeId) ?: return null
        return try {
            val json = assetManager.open(theme.assetPath).bufferedReader().use { it.readText() }
            val scheme = TextMateThemeParser.parseEditorColorScheme(json)
            editorSchemeCache[themeId] = scheme
            scheme
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse editor scheme for $themeId", e)
            null
        }
    }

    fun getComposeColors(assetManager: AssetManager, themeId: String): ComposeThemeColors? {
        composeColorsCache[themeId]?.let { return it }
        val theme = getTheme(themeId) ?: return null
        return try {
            val json = assetManager.open(theme.assetPath).bufferedReader().use { it.readText() }
            val colors = TextMateThemeParser.parseComposeColors(json, theme.isDark)
            composeColorsCache[themeId] = colors
            colors
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse compose colors for $themeId", e)
            null
        }
    }

    fun getCurrentEditorColorScheme(assetManager: AssetManager): EditorColorScheme? {
        return getEditorColorScheme(assetManager, currentThemeId)
    }

    fun getCurrentComposeColors(assetManager: AssetManager): ComposeThemeColors? {
        return getComposeColors(assetManager, currentThemeId)
    }

    fun clearCache() {
        editorSchemeCache.clear()
        composeColorsCache.clear()
    }

    companion object {
        private const val TAG = "ThemeRegistry"
        private const val THEME_INDEX_PATH = "textmate/themes/theme_index.json"
        private const val THEMES_DIR = "textmate/themes"
        const val DEFAULT_THEME_ID = "solarized-dark"

        @Volatile
        private var instance: ThemeRegistry? = null

        fun getInstance(): ThemeRegistry {
            return instance ?: synchronized(this) {
                instance ?: ThemeRegistry().also { instance = it }
            }
        }
    }
}
