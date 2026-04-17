package com.androtext.core.render

import com.androtext.core.buffer.TextBuffer

interface TextRenderer {
    val buffer: TextBuffer

    fun render(canvas: Any)

    fun setFontSize(size: Float)

    fun setFontFamily(family: String)

    fun setShowLineNumbers(show: Boolean)

    fun setLineSpacing(extra: Float, multiplier: Float)

    fun invalidate()
}

interface DecorationProvider {
    val decorations: List<Decoration>

    fun addDecoration(decoration: Decoration)

    fun removeDecoration(decoration: Decoration)

    fun clear()
}

data class Decoration(
    val startOffset: Int,
    val endOffset: Int,
    val type: DecorationType,
    val color: Long? = null,
)

enum class DecorationType {
    SELECTION,
    CARET,
    SEARCH_MATCH,
    ERROR,
    WARNING,
    CUSTOM,
}
