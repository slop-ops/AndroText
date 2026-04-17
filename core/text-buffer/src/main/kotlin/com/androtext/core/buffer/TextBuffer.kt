package com.androtext.core.buffer

interface TextBuffer {
    val length: Int
    val lineCount: Int

    fun getChar(offset: Int): Char

    fun getSlice(start: Int, end: Int): CharSequence

    fun insert(offset: Int, text: CharSequence)

    fun delete(start: Int, end: Int)

    fun replace(start: Int, end: Int, text: CharSequence)

    fun getText(): CharSequence

    fun getLineText(line: Int): CharSequence

    fun getLineStart(line: Int): Int

    fun getLineEnd(line: Int): Int

    fun offsetToLine(offset: Int): Int

    fun addListener(listener: TextChangeListener)

    fun removeListener(listener: TextChangeListener)
}

interface TextChangeListener {
    fun onTextChanged(event: TextChangeEvent)
}

data class TextChangeEvent(
    val startOffset: Int,
    val oldLength: Int,
    val newLength: Int,
    val source: Any? = null,
)
