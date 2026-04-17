package com.androtext.core.lang

import com.androtext.core.buffer.TextBuffer
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage

class TextMateLanguageService(
    override val languageId: String,
    override val displayName: String,
    private val registry: LanguageRegistry,
    private val scopeName: String = "source.$languageId",
) : LanguageService {

    private val fileExtensions = mutableSetOf<String>()

    fun addFileExtension(ext: String): TextMateLanguageService {
        fileExtensions.add(ext.lowercase())
        return this
    }

    fun addFileExtensions(vararg exts: String): TextMateLanguageService {
        exts.forEach { fileExtensions.add(it.lowercase()) }
        return this
    }

    override fun matchesExtension(extension: String): Boolean {
        return fileExtensions.contains(extension.lowercase())
    }

    fun createSoraLanguage(collectIdentifiers: Boolean = true): TextMateLanguage {
        return TextMateLanguage.create(
            scopeName,
            registry.grammarRegistry(),
            registry.themeRegistry(),
            collectIdentifiers,
        )
    }

    fun createIncrementalTokenizer(buffer: TextBuffer): IncrementalTokenizer {
        return IncrementalTokenizer(buffer, createTokenProvider())
    }

    override fun createTokenProvider(): TokenProvider {
        return TextMateTokenProvider(scopeName, registry)
    }
}
