package com.androtext.core.lang

import com.androtext.core.buffer.TextBuffer
import com.androtext.core.buffer.TextChangeEvent
import com.androtext.core.buffer.TextChangeListener

class IncrementalTokenizer(
    private val buffer: TextBuffer,
    private val tokenProvider: TokenProvider,
) : TokenStore, TextChangeListener {

    private val lineTokens: MutableList<List<Token>> = mutableListOf()
    private val lineStates: MutableList<Int> = mutableListOf()
    private val dirtyLines: MutableSet<Int> = mutableSetOf()
    private val listeners = mutableListOf<TokenStoreListener>()

    private var attached = false

    @Volatile
    private var tokenizing = false

    init {
        attach()
    }

    fun attach() {
        if (attached) return
        buffer.addListener(this)
        attached = true
        markAllDirty()
        tokenizeDirty()
    }

    fun detach() {
        if (!attached) return
        buffer.removeListener(this)
        attached = false
    }

    override fun getTokensForLine(line: Int): List<Token> {
        if (line < 0 || line >= lineTokens.size) return emptyList()
        if (line in dirtyLines) tokenizeDirty()
        return lineTokens[line]
    }

    override fun getAllTokens(): List<Token> {
        if (dirtyLines.isNotEmpty()) tokenizeDirty()
        return lineTokens.flatten()
    }

    override val lineCount: Int
        get() = lineTokens.size

    override val isFullyTokenized: Boolean
        get() = dirtyLines.isEmpty()

    override fun addListener(listener: TokenStoreListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TokenStoreListener) {
        listeners.remove(listener)
    }

    override fun onTextChanged(event: TextChangeEvent) {
        if (event.source === this) return

        val startLine = buffer.offsetToLine(event.startOffset)
        markDirty(startLine)
    }

    private fun markAllDirty() {
        dirtyLines.clear()
        for (i in 0 until buffer.lineCount) {
            dirtyLines.add(i)
        }
    }

    private fun markDirty(line: Int) {
        val currentLineCount = buffer.lineCount

        while (lineTokens.size < currentLineCount) {
            lineTokens.add(emptyList())
            lineStates.add(0)
        }
        while (lineTokens.size > currentLineCount) {
            lineTokens.removeAt(lineTokens.size - 1)
            lineStates.removeAt(lineStates.size - 1)
        }

        for (i in line until currentLineCount) {
            dirtyLines.add(i)
        }
    }

    private fun tokenizeDirty() {
        if (tokenizing || dirtyLines.isEmpty()) return
        tokenizing = true

        try {
            val currentLineCount = buffer.lineCount

            while (lineTokens.size < currentLineCount) {
                lineTokens.add(emptyList())
                lineStates.add(0)
            }
            while (lineTokens.size > currentLineCount) {
                lineTokens.removeAt(lineTokens.size - 1)
                lineStates.removeAt(lineStates.size - 1)
            }

            val sortedDirty = dirtyLines.sorted()
            dirtyLines.clear()

            var firstChanged = currentLineCount
            var lastChanged = -1

            for (dirtyLine in sortedDirty) {
                if (dirtyLine >= currentLineCount) continue

                val prevState = if (dirtyLine == 0) {
                    tokenProvider.getInitialState()
                } else {
                    if (dirtyLine - 1 in dirtyLines || dirtyLine - 1 >= lineStates.size) {
                        tokenProvider.getInitialState()
                    } else {
                        lineStates[dirtyLine - 1]
                    }
                }

                val lineStart = buffer.getLineStart(dirtyLine)
                val lineEnd = buffer.getLineEnd(dirtyLine)
                val effectiveEnd = if (lineEnd == -1) buffer.length else lineEnd
                val text = buffer.getSlice(lineStart, effectiveEnd)

                val result = tokenProvider.tokenize(text, lineStart, effectiveEnd, prevState)

                lineTokens[dirtyLine] = result.tokens
                lineStates[dirtyLine] = result.endState

                firstChanged = minOf(firstChanged, dirtyLine)
                lastChanged = maxOf(lastChanged, dirtyLine)

                if (dirtyLine + 1 < currentLineCount) {
                    val nextOldState = if (dirtyLine + 1 < lineStates.size) lineStates[dirtyLine + 1] else -1
                    if (result.endState != nextOldState) {
                        dirtyLines.add(dirtyLine + 1)
                    }
                }
            }

            if (lastChanged >= firstChanged) {
                notifyListeners(firstChanged, lastChanged)
            }
        } finally {
            tokenizing = false
        }
    }

    private fun notifyListeners(startLine: Int, endLine: Int) {
        for (listener in listeners.toList()) {
            listener.onTokensChanged(startLine, endLine)
        }
    }
}
