package com.androtext.core.buffer

class IncrementalLineIndex : LineIndex {

    private val lineStarts: MutableList<Int> = mutableListOf(0)

    override val lineCount: Int
        get() = lineStarts.size

    override fun getLineStart(line: Int): Int {
        if (line < 0 || line >= lineStarts.size) {
            throw IndexOutOfBoundsException("Line $line out of bounds [0, ${lineStarts.size})")
        }
        return lineStarts[line]
    }

    override fun getLineEnd(line: Int): Int {
        if (line < 0 || line >= lineStarts.size) {
            throw IndexOutOfBoundsException("Line $line out of bounds [0, ${lineStarts.size})")
        }
        return if (line + 1 < lineStarts.size) lineStarts[line + 1] else -1
    }

    override fun getLineText(text: CharSequence, line: Int): CharSequence {
        val start = getLineStart(line)
        val end = getLineEnd(line)
        val effectiveEnd = if (end == -1) text.length else end
        val slice = text.subSequence(start, effectiveEnd)
        return if (slice.isNotEmpty() && slice[slice.length - 1] == '\n') {
            slice.subSequence(0, slice.length - 1)
        } else {
            slice
        }
    }

    override fun offsetToLine(offset: Int): Int {
        if (offset < 0) throw IllegalArgumentException("Offset must be >= 0")
        if (lineStarts.isEmpty()) return 0

        var low = 0
        var high = lineStarts.size - 1

        while (low < high) {
            val mid = (low + high + 1) ushr 1
            if (lineStarts[mid] <= offset) {
                low = mid
            } else {
                high = mid - 1
            }
        }

        return low
    }

    override fun onInserted(offset: Int, insertedLength: Int, text: CharSequence) {
        val lineIdx = offsetToLine(offset)
        val newLinesInInsert = countNewlines(text)

        if (newLinesInInsert == 0) {
            for (i in (lineIdx + 1) until lineStarts.size) {
                lineStarts[i] += insertedLength
            }
        } else {
            val baseOffset = lineStarts[lineIdx]
            val newLineOffsets = mutableListOf<Int>()

            var localOffset = offset - baseOffset
            for (i in text.indices) {
                if (text[i] == '\n') {
                    newLineOffsets.add(baseOffset + localOffset + i + 1)
                }
            }

            for (i in (lineIdx + 1) until lineStarts.size) {
                lineStarts[i] += insertedLength
            }

            for ((j, lineOffset) in newLineOffsets.withIndex()) {
                lineStarts.add(lineIdx + 1 + j, lineOffset)
            }
        }
    }

    override fun onDeleted(offset: Int, deletedLength: Int, text: CharSequence) {
        val startLine = offsetToLine(offset)
        val endLine = offsetToLine(offset + deletedLength)

        if (startLine == endLine) {
            for (i in (startLine + 1) until lineStarts.size) {
                lineStarts[i] -= deletedLength
            }
        } else {
            val linesToRemove = endLine - startLine
            val newLinesInDeleted = countNewlines(text)

            repeat(linesToRemove) {
                if (startLine + 1 < lineStarts.size) {
                    lineStarts.removeAt(startLine + 1)
                }
            }

            for (i in (startLine + 1) until lineStarts.size) {
                lineStarts[i] -= deletedLength
            }
        }
    }

    private fun countNewlines(text: CharSequence): Int {
        var count = 0
        for (i in text.indices) {
            if (text[i] == '\n') count++
        }
        return count
    }
}
