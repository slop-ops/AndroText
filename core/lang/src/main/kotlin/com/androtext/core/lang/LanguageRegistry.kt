package com.androtext.core.lang

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.DefaultGrammarDefinition
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource

class LanguageRegistry private constructor() {

    private val grammarRegistry: GrammarRegistry = GrammarRegistry.getInstance()
    private val themeRegistry: ThemeRegistry = ThemeRegistry.getInstance()
    private val fileProviderRegistry: FileProviderRegistry = FileProviderRegistry.getInstance()

    private val loadedLanguages = mutableMapOf<String, LanguageService>()
    private val extensionToScope = mutableMapOf<String, String>()
    private var isInitialized = false

    fun initialize(context: Context) {
        initialize(context.assets)
    }

    fun initialize(assetManager: AssetManager) {
        if (isInitialized) return
        fileProviderRegistry.addFileProvider(AssetsFileResolver(assetManager))
        isInitialized = true
    }

    fun registerAllLanguages() {
        for (def in LanguageDefinitions.getAll()) {
            try {
                registerLanguage(def)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register language: ${def.name}", e)
            }
        }
    }

    fun loadAllThemes(assetManager: AssetManager) {
        try {
            val index = assetManager.open("textmate/themes/theme_index.json").bufferedReader().use { it.readText() }
            val arr = org.json.JSONArray(index)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.getString("id")
                val fileName = obj.getString("fileName")
                val isDark = obj.getBoolean("isDark")
                try {
                    loadTheme("textmate/themes/$fileName", id, isDark)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load theme: $id", e)
                }
            }
            try {
                themeRegistry.setTheme("solarized-dark")
            } catch (_: Exception) {
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load theme index", e)
            loadTheme("textmate/themes/solarized-dark.json", "solarized-dark", true)
        }
    }

    fun setActiveTheme(themeId: String) {
        try {
            themeRegistry.setTheme(themeId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set active theme: $themeId", e)
        }
    }

    private fun registerLanguage(def: GrammarDefinition): TextMateLanguageService {
        val inputStream = fileProviderRegistry.tryGetInputStream(def.grammarPath)
            ?: throw IllegalArgumentException("Grammar file not found: ${def.grammarPath}")

        val grammarSource = IGrammarSource.fromInputStream(
            inputStream, def.grammarPath, null,
        )

        val definition = if (def.languageConfigurationPath != null) {
            DefaultGrammarDefinition.withLanguageConfiguration(
                grammarSource,
                def.languageConfigurationPath,
                def.name,
                def.scopeName,
            )
        } else {
            DefaultGrammarDefinition.withGrammarSource(
                grammarSource, def.name, def.scopeName,
            )
        }

        grammarRegistry.loadGrammar(definition)

        val service = TextMateLanguageService(def.name, def.scopeName, this)
        service.addFileExtensions(*def.extensions.toTypedArray())
        loadedLanguages[def.scopeName] = service

        for (ext in def.extensions) {
            extensionToScope[ext.lowercase()] = def.scopeName
        }

        return service
    }

    fun registerLanguage(
        name: String,
        scopeName: String,
        grammarPath: String,
        languageConfigurationPath: String? = null,
    ): TextMateLanguageService {
        val inputStream = fileProviderRegistry.tryGetInputStream(grammarPath)
            ?: throw IllegalArgumentException("Grammar file not found: $grammarPath")

        val grammarSource = IGrammarSource.fromInputStream(inputStream, grammarPath, null)

        val definition = if (languageConfigurationPath != null) {
            DefaultGrammarDefinition.withLanguageConfiguration(
                grammarSource,
                languageConfigurationPath,
                name,
                scopeName,
            )
        } else {
            DefaultGrammarDefinition.withGrammarSource(grammarSource, name, scopeName)
        }

        grammarRegistry.loadGrammar(definition)

        val service = TextMateLanguageService(name, scopeName, this)
        loadedLanguages[scopeName] = service
        return service
    }

    fun loadTheme(themePath: String, themeName: String, isDark: Boolean = false) {
        val inputStream = fileProviderRegistry.tryGetInputStream(themePath)
            ?: throw IllegalArgumentException("Theme file not found: $themePath")

        val themeSource = IThemeSource.fromInputStream(inputStream, themePath, null)
        val themeModel = ThemeModel(themeSource, themeName)
        themeModel.setDark(isDark)
        themeRegistry.loadTheme(themeModel)
    }

    fun getLanguage(scopeName: String): LanguageService? = loadedLanguages[scopeName]

    fun getLanguageForFile(fileName: String): LanguageService? {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val scopeName = extensionToScope[extension] ?: return null
        return loadedLanguages[scopeName]
    }

    fun grammarRegistry(): GrammarRegistry = grammarRegistry

    fun themeRegistry(): ThemeRegistry = themeRegistry

    fun dispose() {
        loadedLanguages.clear()
        extensionToScope.clear()
        grammarRegistry.dispose()
        fileProviderRegistry.dispose()
        isInitialized = false
    }

    companion object {
        private const val TAG = "LanguageRegistry"

        @Volatile
        private var instance: LanguageRegistry? = null

        fun getInstance(): LanguageRegistry {
            return instance ?: synchronized(this) {
                instance ?: LanguageRegistry().also { instance = it }
            }
        }
    }
}
