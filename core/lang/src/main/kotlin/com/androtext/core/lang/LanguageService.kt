package com.androtext.core.lang

data class Token(
    val startOffset: Int,
    val endOffset: Int,
    val type: TokenType,
    val scope: String,
)

enum class TokenType {
    KEYWORD,
    STRING,
    COMMENT,
    NUMBER,
    IDENTIFIER,
    OPERATOR,
    PUNCTUATION,
    TYPE,
    FUNCTION,
    VARIABLE,
    PROPERTY,
    ANNOTATION,
    WHITESPACE,
    UNKNOWN,
}

interface TokenProvider {
    fun tokenize(
        text: CharSequence,
        startOffset: Int,
        endOffset: Int,
        previousState: Int,
    ): TokenizeResult

    fun invalidateRange(startOffset: Int, endOffset: Int)

    fun getInitialState(): Int
}

data class TokenizeResult(
    val tokens: List<Token>,
    val endState: Int,
)

interface LanguageService {
    val languageId: String
    val displayName: String

    fun createTokenProvider(): TokenProvider

    fun matchesExtension(extension: String): Boolean
}
