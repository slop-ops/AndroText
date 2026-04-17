package com.androtext.core.lang

class TextMateTokenProvider(
    private val scopeName: String,
    private val registry: LanguageRegistry,
) : TokenProvider {

    private var currentState: Int = 0
    private var invalidatedStart: Int = -1
    private var invalidatedEnd: Int = -1

    override fun tokenize(
        text: CharSequence,
        startOffset: Int,
        endOffset: Int,
        previousState: Int,
    ): TokenizeResult {
        val grammar = registry.grammarRegistry().findGrammar(scopeName)
        if (grammar == null) {
            return TokenizeResult(emptyList(), 0)
        }

        val tokens = mutableListOf<Token>()
        val lineText = text.subSequence(startOffset, endOffset)
        val simpleTokens = tokenizeSimple(lineText)

        var offset = startOffset
        for (tokenText in simpleTokens) {
            val type = classifyToken(tokenText.toString())
            tokens.add(
                Token(
                    startOffset = offset,
                    endOffset = offset + tokenText.length,
                    type = type,
                    scope = scopeForType(type),
                )
            )
            offset += tokenText.length
        }

        currentState = previousState + 1
        return TokenizeResult(tokens, currentState)
    }

    override fun invalidateRange(startOffset: Int, endOffset: Int) {
        invalidatedStart = startOffset
        invalidatedEnd = endOffset
    }

    override fun getInitialState(): Int = 0

    private fun tokenizeSimple(text: CharSequence): List<CharSequence> {
        if (text.isEmpty()) return emptyList()
        val tokens = mutableListOf<CharSequence>()
        var start = 0
        var i = 0

        while (i < text.length) {
            val c = text[i]
            if (c.isWhitespace()) {
                if (i > start) tokens.add(text.subSequence(start, i))
                start = i + 1
                i++
            } else if (c in OPERATOR_CHARS) {
                if (i > start) tokens.add(text.subSequence(start, i))
                tokens.add(text.subSequence(i, i + 1))
                start = i + 1
                i++
            } else if (c == '"' || c == '\'') {
                if (i > start) tokens.add(text.subSequence(start, i))
                val quote = c
                var j = i + 1
                while (j < text.length && text[j] != quote) {
                    if (text[j] == '\\' && j + 1 < text.length) j++
                    j++
                }
                if (j < text.length) j++
                tokens.add(text.subSequence(i, j))
                start = j
                i = j
            } else {
                i++
            }
        }
        if (start < text.length) {
            tokens.add(text.subSequence(start, text.length))
        }
        return tokens
    }

    private fun classifyToken(text: String): TokenType = when {
        text.isBlank() -> TokenType.WHITESPACE
        text.startsWith("//") || text.startsWith("/*") || text.startsWith("#") -> TokenType.COMMENT
        text.startsWith("\"") || text.startsWith("'") -> TokenType.STRING
        text.all { it.isDigit() } || text.matches(HEX_PATTERN) -> TokenType.NUMBER
        text.length == 1 && text in OPERATOR_CHARS.toString() -> TokenType.OPERATOR
        text.first().isLetter() || text.first() == '_' -> when {
            text == text.uppercase() && text.any { it.isLetter() } -> TokenType.TYPE
            else -> TokenType.IDENTIFIER
        }
        else -> TokenType.UNKNOWN
    }

    private fun scopeForType(type: TokenType): String = when (type) {
        TokenType.KEYWORD -> "$scopeName.keyword"
        TokenType.STRING -> "$scopeName.string"
        TokenType.COMMENT -> "$scopeName.comment"
        TokenType.NUMBER -> "$scopeName.constant.numeric"
        TokenType.IDENTIFIER -> "$scopeName.identifier"
        TokenType.OPERATOR -> "$scopeName.operator"
        TokenType.PUNCTUATION -> "$scopeName.punctuation"
        TokenType.TYPE -> "$scopeName.type"
        TokenType.FUNCTION -> "$scopeName.function"
        TokenType.VARIABLE -> "$scopeName.variable"
        TokenType.PROPERTY -> "$scopeName.property"
        TokenType.ANNOTATION -> "$scopeName.annotation"
        TokenType.WHITESPACE -> "$scopeName.whitespace"
        TokenType.UNKNOWN -> "$scopeName"
    }

    companion object {
        private const val OPERATOR_CHARS = "+-*/%=<>!&|^~?:;,."
        private val HEX_PATTERN = Regex("^0[xX][0-9a-fA-F]+$")
    }
}
