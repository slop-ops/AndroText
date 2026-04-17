package com.androtext.core.lang

import android.content.Context
import android.content.res.AssetManager
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

    fun initialize(context: Context) {
        fileProviderRegistry.addFileProvider(AssetsFileResolver(context.assets))
    }

    fun initialize(assetManager: AssetManager) {
        fileProviderRegistry.addFileProvider(AssetsFileResolver(assetManager))
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
        return loadedLanguages.values.find { it.matchesExtension(extension) }
    }

    fun grammarRegistry(): GrammarRegistry = grammarRegistry

    fun themeRegistry(): ThemeRegistry = themeRegistry

    fun dispose() {
        loadedLanguages.clear()
        grammarRegistry.dispose()
        fileProviderRegistry.dispose()
    }

    companion object {
        @Volatile
        private var instance: LanguageRegistry? = null

        fun getInstance(): LanguageRegistry {
            return instance ?: synchronized(this) {
                instance ?: LanguageRegistry().also { instance = it }
            }
        }
    }
}
