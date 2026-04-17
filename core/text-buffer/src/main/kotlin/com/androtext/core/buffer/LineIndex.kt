package com.androtext.core.buffer

interface LineIndex {
    val lineCount: Int

    fun getLineStart(line: Int): Int

    fun getLineEnd(line: Int): Int

    fun getLineText(text: CharSequence, line: Int): CharSequence

    fun offsetToLine(offset: Int): Int

    fun onInserted(offset: Int, insertedLength: Int, text: CharSequence)

    fun onDeleted(offset: Int, deletedLength: Int, text: CharSequence)
}
