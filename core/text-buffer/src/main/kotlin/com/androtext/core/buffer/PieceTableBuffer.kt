package com.androtext.core.buffer

import java.util.concurrent.CopyOnWriteArrayList

class PieceTableBuffer(initialText: String = "") : TextBuffer {

    private data class Piece(
        val bufferId: Int,
        val start: Int,
        val length: Int,
    )

    private companion object {
        const val ORIGINAL_BUFFER = 0
        const val ADD_BUFFER = 1
    }

    private val originalBuffer: StringBuilder = StringBuilder(initialText)
    private val addBuffer: StringBuilder = StringBuilder()

    private val pieces: MutableList<Piece> = mutableListOf()
    private val listeners: CopyOnWriteArrayList<TextChangeListener> = CopyOnWriteArrayList()

    private var _lineIndex: LineIndex? = null

    var lineIndex: LineIndex?
        get() = _lineIndex
        set(value) {
            _lineIndex = value
            if (value != null && length > 0) {
                value.onInserted(0, length, getText())
            }
        }

    init {
        if (initialText.isNotEmpty()) {
            pieces.add(Piece(ORIGINAL_BUFFER, 0, initialText.length))
        }
    }

    override val length: Int
        get() = pieces.sumOf { it.length }

    override val lineCount: Int
        get() {
            val li = _lineIndex
            if (li != null) return li.lineCount
            var count = 1
            val text = getText()
            for (i in text.indices) {
                if (text[i] == '\n') count++
            }
            return count
        }

    override fun getChar(offset: Int): Char {
        require(offset in 0 until length) { "Offset $offset out of bounds [0, $length)" }
        val (piece, localOffset) = findPieceAt(offset)
        return getBufferChar(piece.bufferId, piece.start + localOffset)
    }

    override fun getSlice(start: Int, end: Int): CharSequence {
        require(start in 0..length) { "Start $start out of bounds [0, $length]" }
        require(end in start..length) { "End $end out of bounds [$start, $length]" }
        if (start == end) return ""

        val result = StringBuilder(end - start)
        var remaining = end - start

        val (pieceIdx, localOffset) = findPieceIndexAt(start)
        var idx = pieceIdx
        var fromLocal = localOffset

        while (idx < pieces.size && remaining > 0) {
            val piece = pieces[idx]
            val availableInPiece = piece.length - fromLocal
            val toRead = minOf(availableInPiece, remaining)
            val buf = getBuffer(piece.bufferId)
            result.append(buf, piece.start + fromLocal, piece.start + fromLocal + toRead)
            remaining -= toRead
            fromLocal = 0
            idx++
        }

        return result
    }

    override fun insert(offset: Int, text: CharSequence) {
        require(offset in 0..length) { "Offset $offset out of bounds [0, $length]" }
        if (text.isEmpty()) return

        val addStart = addBuffer.length
        addBuffer.append(text)

        val newPiece = Piece(ADD_BUFFER, addStart, text.length)

        if (pieces.isEmpty()) {
            pieces.add(newPiece)
        } else {
            val (pieceIdx, localOffset) = findPieceIndexAt(offset)

            when {
                localOffset == 0 -> {
                    pieces.add(pieceIdx, newPiece)
                }
                localOffset == pieces[pieceIdx].length -> {
                    pieces.add(pieceIdx + 1, newPiece)
                }
                else -> {
                    val original = pieces[pieceIdx]
                    val left = Piece(original.bufferId, original.start, localOffset)
                    val right = Piece(
                        original.bufferId,
                        original.start + localOffset,
                        original.length - localOffset,
                    )
                    pieces[pieceIdx] = left
                    pieces.add(pieceIdx + 1, newPiece)
                    pieces.add(pieceIdx + 2, right)
                }
            }
        }

        _lineIndex?.onInserted(offset, text.length, text)
        notifyListeners(TextChangeEvent(offset, 0, text.length))
    }

    override fun delete(start: Int, end: Int) {
        require(start in 0..length) { "Start $start out of bounds [0, $length]" }
        require(end in start..length) { "End $end out of bounds [$start, $length]" }
        if (start == end) return

        val deletedText = _lineIndex?.let { getSlice(start, end) }
        val deletedLength = end - start

        val replacement = mutableListOf<Piece>()
        var currentOffset = 0

        for (piece in pieces) {
            val pieceStart = currentOffset
            val pieceEnd = currentOffset + piece.length

            if (pieceEnd <= start || pieceStart >= end) {
                replacement.add(piece)
            } else {
                val overlapStart = maxOf(pieceStart, start) - pieceStart
                val overlapEnd = minOf(pieceEnd, end) - pieceStart

                if (overlapStart > 0) {
                    replacement.add(Piece(piece.bufferId, piece.start, overlapStart))
                }
                if (overlapEnd < piece.length) {
                    replacement.add(
                        Piece(
                            piece.bufferId,
                            piece.start + overlapEnd,
                            piece.length - overlapEnd,
                        )
                    )
                }
            }

            currentOffset += piece.length
        }

        pieces.clear()
        pieces.addAll(replacement)

        _lineIndex?.onDeleted(start, deletedLength, deletedText ?: "")
        notifyListeners(TextChangeEvent(start, deletedLength, 0))
    }

    override fun replace(start: Int, end: Int, text: CharSequence) {
        require(start in 0..length) { "Start $start out of bounds [0, $length]" }
        require(end in start..length) { "End $end out of bounds [$start, $length]" }
        delete(start, end)
        insert(start, text)
    }

    override fun getText(): CharSequence {
        if (pieces.isEmpty()) return ""
        val result = StringBuilder(length)
        for (piece in pieces) {
            val buf = getBuffer(piece.bufferId)
            result.append(buf, piece.start, piece.start + piece.length)
        }
        return result
    }

    override fun getLineText(line: Int): CharSequence {
        val li = _lineIndex
        require(line >= 0) { "Line $line must be >= 0" }
        if (li != null) {
            require(line < li.lineCount) { "Line $line out of bounds [0, ${li.lineCount})" }
            val start = li.getLineStart(line)
            val end = li.getLineEnd(line)
            val effectiveEnd = if (end == -1) length else end
            val slice = getSlice(start, effectiveEnd)
            return if (slice.isNotEmpty() && slice[slice.length - 1] == '\n') {
                slice.subSequence(0, slice.length - 1)
            } else {
                slice
            }
        }
        val text = getText()
        var currentLine = 0
        var lineStart = 0
        for (i in text.indices) {
            if (currentLine == line) {
                if (text[i] == '\n') return text.subSequence(lineStart, i)
            } else if (text[i] == '\n') {
                currentLine++
                if (currentLine == line) lineStart = i + 1
            }
        }
        if (currentLine == line) return text.subSequence(lineStart, text.length)
        throw IndexOutOfBoundsException("Line $line out of bounds [0, ${currentLine + 1})")
    }

    override fun getLineStart(line: Int): Int {
        val li = _lineIndex
        if (li != null) return li.getLineStart(line)
        return slowGetLineStart(line)
    }

    override fun getLineEnd(line: Int): Int {
        val li = _lineIndex
        if (li != null) {
            val end = li.getLineEnd(line)
            return if (end == -1) length else end
        }
        val start = slowGetLineStart(line)
        val text = getText()
        for (i in start until text.length) {
            if (text[i] == '\n') return i + 1
        }
        return text.length
    }

    override fun offsetToLine(offset: Int): Int {
        val li = _lineIndex
        if (li != null) return li.offsetToLine(offset)
        val text = getText()
        var line = 0
        for (i in 0 until minOf(offset, text.length)) {
            if (text[i] == '\n') line++
        }
        return line
    }

    override fun addListener(listener: TextChangeListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TextChangeListener) {
        listeners.remove(listener)
    }

    private fun findPieceAt(offset: Int): Pair<Piece, Int> {
        var currentOffset = 0
        for (piece in pieces) {
            if (offset < currentOffset + piece.length) {
                return Pair(piece, offset - currentOffset)
            }
            currentOffset += piece.length
        }
        throw IndexOutOfBoundsException("Offset $offset beyond buffer length $length")
    }

    private fun findPieceIndexAt(offset: Int): Pair<Int, Int> {
        if (offset == length) {
            if (pieces.isEmpty()) return Pair(0, 0)
            val lastIdx = pieces.size - 1
            return Pair(lastIdx, pieces[lastIdx].length)
        }
        var currentOffset = 0
        for ((idx, piece) in pieces.withIndex()) {
            if (offset < currentOffset + piece.length) {
                return Pair(idx, offset - currentOffset)
            }
            currentOffset += piece.length
        }
        throw IndexOutOfBoundsException("Offset $offset beyond buffer length $length")
    }

    private fun getBuffer(bufferId: Int): CharSequence = when (bufferId) {
        ORIGINAL_BUFFER -> originalBuffer
        ADD_BUFFER -> addBuffer
        else -> throw IllegalArgumentException("Unknown buffer ID: $bufferId")
    }

    private fun getBufferChar(bufferId: Int, index: Int): Char = when (bufferId) {
        ORIGINAL_BUFFER -> originalBuffer[index]
        ADD_BUFFER -> addBuffer[index]
        else -> throw IllegalArgumentException("Unknown buffer ID: $bufferId")
    }

    private fun slowGetLineStart(line: Int): Int {
        val text = getText()
        var currentLine = 0
        for (i in text.indices) {
            if (currentLine == line) return i
            if (text[i] == '\n') currentLine++
        }
        if (currentLine == line) return text.length
        throw IndexOutOfBoundsException("Line $line out of bounds")
    }

    private fun notifyListeners(event: TextChangeEvent) {
        for (listener in listeners) {
            listener.onTextChanged(event)
        }
    }
}
