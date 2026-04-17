package com.androtext.core.buffer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IncrementalLineIndexTest {

    @Test
    fun `single line - no newlines`() {
        val li = IncrementalLineIndex()
        assertEquals(1, li.lineCount)
        assertEquals(0, li.getLineStart(0))
    }

    @Test
    fun `offsetToLine on single line`() {
        val li = IncrementalLineIndex()
        assertEquals(0, li.offsetToLine(0))
        assertEquals(0, li.offsetToLine(5))
    }

    @Test
    fun `insert with no newlines shifts existing offsets`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 5, "hello")
        assertEquals(1, li.lineCount)
        assertEquals(0, li.offsetToLine(0))
        assertEquals(0, li.offsetToLine(4))

        li.onInserted(5, 6, " world")
        assertEquals(1, li.lineCount)
        assertEquals(0, li.offsetToLine(10))
    }

    @Test
    fun `insert with newline creates new line`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 6, "hello\n")
        assertEquals(2, li.lineCount)
        assertEquals(0, li.getLineStart(0))
        assertEquals(6, li.getLineStart(1))
    }

    @Test
    fun `multiline insert`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 12, "line1\nline2\n")
        assertEquals(3, li.lineCount)
        assertEquals(0, li.getLineStart(0))
        assertEquals(6, li.getLineStart(1))
        assertEquals(12, li.getLineStart(2))
    }

    @Test
    fun `offsetToLine on multiline`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 17, "line1\nline2\nline3")
        assertEquals(3, li.lineCount)
        assertEquals(0, li.offsetToLine(0))
        assertEquals(0, li.offsetToLine(5))
        assertEquals(1, li.offsetToLine(6))
        assertEquals(1, li.offsetToLine(11))
        assertEquals(2, li.offsetToLine(12))
        assertEquals(2, li.offsetToLine(16))
    }

    @Test
    fun `delete within single line`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 12, "line1\nline2\n")
        assertEquals(3, li.lineCount)

        li.onDeleted(6, 3, "lin")
        assertEquals(3, li.lineCount)
        assertEquals(0, li.getLineStart(0))
        assertEquals(6, li.getLineStart(1))
        assertEquals(9, li.getLineStart(2))
    }

    @Test
    fun `delete across line boundary removes lines`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 12, "line1\nline2\n")
        assertEquals(3, li.lineCount)

        li.onDeleted(5, 2, "\nl")
        assertEquals(2, li.lineCount)
        assertEquals(0, li.getLineStart(0))
        assertEquals(10, li.getLineStart(1))
    }

    @Test
    fun `getLineText returns text without trailing newline`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 17, "line1\nline2\nline3")
        assertEquals("line1", li.getLineText("line1\nline2\nline3", 0).toString())
        assertEquals("line2", li.getLineText("line1\nline2\nline3", 1).toString())
        assertEquals("line3", li.getLineText("line1\nline2\nline3", 2).toString())
    }

    @Test
    fun `integrated with PieceTableBuffer`() {
        val li = IncrementalLineIndex()
        val buf = PieceTableBuffer("line1\nline2\nline3")
        buf.lineIndex = li

        assertEquals(3, buf.lineCount)
        assertEquals("line1", buf.getLineText(0).toString())
        assertEquals("line2", buf.getLineText(1).toString())
        assertEquals("line3", buf.getLineText(2).toString())
        assertEquals(0, buf.getLineStart(0))
        assertEquals(6, buf.getLineStart(1))
        assertEquals(12, buf.getLineStart(2))
    }

    @Test
    fun `integrated - insert creates new line`() {
        val li = IncrementalLineIndex()
        val buf = PieceTableBuffer("hello world")
        buf.lineIndex = li

        buf.insert(5, "\n")
        assertEquals(2, buf.lineCount)
        assertEquals("hello", buf.getLineText(0).toString())
        assertEquals(" world", buf.getLineText(1).toString())
    }

    @Test
    fun `integrated - delete removes line`() {
        val li = IncrementalLineIndex()
        val buf = PieceTableBuffer("line1\nline2\nline3")
        buf.lineIndex = li

        buf.delete(6, 12)
        assertEquals(2, buf.lineCount)
        assertEquals("line1", buf.getLineText(0).toString())
        assertEquals("line3", buf.getLineText(1).toString())
    }

    @Test
    fun `integrated - offsetToLine`() {
        val li = IncrementalLineIndex()
        val buf = PieceTableBuffer("line1\nline2\nline3")
        buf.lineIndex = li

        assertEquals(0, buf.offsetToLine(0))
        assertEquals(0, buf.offsetToLine(5))
        assertEquals(1, buf.offsetToLine(6))
        assertEquals(2, buf.offsetToLine(12))
    }

    @Test
    fun `integrated - replace`() {
        val li = IncrementalLineIndex()
        val buf = PieceTableBuffer("aaa\nbbb\nccc")
        buf.lineIndex = li

        buf.replace(4, 7, "XX")
        assertEquals("aaa\nXX\nccc", buf.getText().toString())
        assertEquals(3, buf.lineCount)
    }

    @Test
    fun `empty buffer has one line`() {
        val li = IncrementalLineIndex()
        assertEquals(1, li.lineCount)
        assertEquals(0, li.getLineStart(0))
    }

    @Test
    fun `getLineEnd returns -1 for last line`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 5, "hello")
        assertEquals(-1, li.getLineEnd(0))
    }

    @Test
    fun `getLineEnd returns next line start for non-last line`() {
        val li = IncrementalLineIndex()
        li.onInserted(0, 12, "hello\nworld\n")
        assertEquals(6, li.getLineEnd(0))
        assertEquals(12, li.getLineEnd(1))
        assertEquals(-1, li.getLineEnd(2))
    }
}
