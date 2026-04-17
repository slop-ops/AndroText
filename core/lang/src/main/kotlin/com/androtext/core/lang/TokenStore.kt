package com.androtext.core.lang

interface TokenStore {
    fun getTokensForLine(line: Int): List<Token>

    fun getAllTokens(): List<Token>

    val lineCount: Int

    val isFullyTokenized: Boolean

    fun addListener(listener: TokenStoreListener)

    fun removeListener(listener: TokenStoreListener)
}

fun interface TokenStoreListener {
    fun onTokensChanged(startLine: Int, endLine: Int)
}
