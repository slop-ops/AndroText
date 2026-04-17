package com.androtext.core.buffer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PieceTableBufferTest {

    @Test
    fun `empty buffer has zero length`() {
        val buf = PieceTableBuffer()
        assertEquals(0, buf.length)
        assertEquals("", buf.getText())
    }

    @Test
    fun `initial text is preserved`() {
        val buf = PieceTableBuffer("hello")
        assertEquals(5, buf.length)
        assertEquals("hello", buf.getText().toString())
    }

    @Test
    fun `getChar returns correct characters`() {
        val buf = PieceTableBuffer("hello")
        assertEquals('h', buf.getChar(0))
        assertEquals('e', buf.getChar(1))
        assertEquals('o', buf.getChar(4))
    }

    @Test
    fun `getSlice returns correct substring`() {
        val buf = PieceTableBuffer("hello world")
        assertEquals("hello", buf.getSlice(0, 5).toString())
        assertEquals("world", buf.getSlice(6, 11).toString())
        assertEquals("lo wo", buf.getSlice(3, 8).toString())
    }

    @Test
    fun `getSlice with empty range returns empty string`() {
        val buf = PieceTableBuffer("hello")
        assertEquals("", buf.getSlice(0, 0).toString())
        assertEquals("", buf.getSlice(3, 3).toString())
    }

    @Test
    fun `insert at beginning`() {
        val buf = PieceTableBuffer("world")
        buf.insert(0, "hello ")
        assertEquals("hello world", buf.getText().toString())
        assertEquals(11, buf.length)
    }

    @Test
    fun `insert at end`() {
        val buf = PieceTableBuffer("hello")
        buf.insert(5, " world")
        assertEquals("hello world", buf.getText().toString())
    }

    @Test
    fun `insert in middle`() {
        val buf = PieceTableBuffer("helo")
        buf.insert(2, "l")
        assertEquals("hello", buf.getText().toString())
    }

    @Test
    fun `multiple inserts`() {
        val buf = PieceTableBuffer()
        buf.insert(0, "a")
        buf.insert(1, "b")
        buf.insert(2, "c")
        assertEquals("abc", buf.getText().toString())
    }

    @Test
    fun `insert into empty buffer`() {
        val buf = PieceTableBuffer()
        buf.insert(0, "hello")
        assertEquals("hello", buf.getText().toString())
    }

    @Test
    fun `delete from beginning`() {
        val buf = PieceTableBuffer("hello world")
        buf.delete(0, 6)
        assertEquals("world", buf.getText().toString())
    }

    @Test
    fun `delete from end`() {
        val buf = PieceTableBuffer("hello world")
        buf.delete(5, 11)
        assertEquals("hello", buf.getText().toString())
    }

    @Test
    fun `delete from middle`() {
        val buf = PieceTableBuffer("hello world")
        buf.delete(5, 6)
        assertEquals("helloworld", buf.getText().toString())
    }

    @Test
    fun `delete entire buffer`() {
        val buf = PieceTableBuffer("hello")
        buf.delete(0, 5)
        assertEquals("", buf.getText().toString())
        assertEquals(0, buf.length)
    }

    @Test
    fun `delete with no-op`() {
        val buf = PieceTableBuffer("hello")
        buf.delete(3, 3)
        assertEquals("hello", buf.getText().toString())
    }

    @Test
    fun `replace text`() {
        val buf = PieceTableBuffer("hello world")
        buf.replace(6, 11, "there")
        assertEquals("hello there", buf.getText().toString())
    }

    @Test
    fun `replace with shorter text`() {
        val buf = PieceTableBuffer("hello world")
        buf.replace(6, 11, "me")
        assertEquals("hello me", buf.getText().toString())
    }

    @Test
    fun `replace with longer text`() {
        val buf = PieceTableBuffer("hi")
        buf.replace(0, 2, "hello")
        assertEquals("hello", buf.getText().toString())
    }

    @Test
    fun `getChar after multiple edits`() {
        val buf = PieceTableBuffer("abc")
        buf.insert(1, "X")
        assertEquals('a', buf.getChar(0))
        assertEquals('X', buf.getChar(1))
        assertEquals('b', buf.getChar(2))
        assertEquals('c', buf.getChar(3))
    }

    @Test
    fun `line operations on multiline text`() {
        val buf = PieceTableBuffer("line1\nline2\nline3")
        assertEquals(3, buf.lineCount)
        assertEquals("line1", buf.getLineText(0).toString())
        assertEquals("line2", buf.getLineText(1).toString())
        assertEquals("line3", buf.getLineText(2).toString())
        assertEquals(0, buf.getLineStart(0))
        assertEquals(6, buf.getLineStart(1))
        assertEquals(12, buf.getLineStart(2))
    }

    @Test
    fun `offsetToLine`() {
        val buf = PieceTableBuffer("line1\nline2\nline3")
        assertEquals(0, buf.offsetToLine(0))
        assertEquals(0, buf.offsetToLine(5))
        assertEquals(1, buf.offsetToLine(6))
        assertEquals(1, buf.offsetToLine(11))
        assertEquals(2, buf.offsetToLine(12))
    }

    @Test
    fun `change listeners are notified`() {
        val buf = PieceTableBuffer("hello")
        val events = mutableListOf<TextChangeEvent>()
        buf.addListener(object : TextChangeListener {
            override fun onTextChanged(event: TextChangeEvent) {
                events.add(event)
            }
        })

        buf.insert(5, " world")
        assertEquals(1, events.size)
        assertEquals(TextChangeEvent(5, 0, 6), events[0])

        buf.delete(0, 6)
        assertEquals(2, events.size)
        assertEquals(TextChangeEvent(0, 6, 0), events[1])
    }

    @Test
    fun `listener can be removed`() {
        val buf = PieceTableBuffer("hello")
        val events = mutableListOf<TextChangeEvent>()
        val listener = object : TextChangeListener {
            override fun onTextChanged(event: TextChangeEvent) {
                events.add(event)
            }
        }

        buf.addListener(listener)
        buf.insert(5, " world")
        assertEquals(1, events.size)

        buf.removeListener(listener)
        buf.delete(0, 6)
        assertEquals(1, events.size)
    }

    @Test
    fun `insert after delete`() {
        val buf = PieceTableBuffer("abcdef")
        buf.delete(2, 4)
        assertEquals("abef", buf.getText().toString())
        buf.insert(2, "CD")
        assertEquals("abCDef", buf.getText().toString())
    }

    @Test
    fun `stress test - sequential inserts`() {
        val buf = PieceTableBuffer()
        val sb = StringBuilder()
        for (i in 0 until 1000) {
            val ch = ('a' + (i % 26))
            buf.insert(buf.length, ch.toString())
            sb.append(ch)
        }
        assertEquals(sb.toString(), buf.getText().toString())
        assertEquals(1000, buf.length)
    }

    @Test
    fun `stress test - sequential deletes`() {
        val buf = PieceTableBuffer("abcdefghij")
        buf.delete(0, 1)
        assertEquals("bcdefghij", buf.getText().toString())
        buf.delete(buf.length - 1, buf.length)
        assertEquals("bcdefghi", buf.getText().toString())
    }

    @Test
    fun `delete across piece boundaries`() {
        val buf = PieceTableBuffer("hello")
        buf.insert(5, " ")
        buf.insert(6, "world")
        assertEquals("hello world", buf.getText().toString())
        buf.delete(4, 7)
        assertEquals("hellorld", buf.getText().toString())
    }

    @Test
    fun `getSlice across piece boundaries`() {
        val buf = PieceTableBuffer("hello")
        buf.insert(5, " ")
        buf.insert(6, "world")
        assertEquals("o w", buf.getSlice(4, 7).toString())
    }

    @Test
    fun `empty insert is no-op`() {
        val buf = PieceTableBuffer("hello")
        buf.insert(3, "")
        assertEquals("hello", buf.getText().toString())
        assertEquals(5, buf.length)
    }

    @Test
    fun `multiline line operations with trailing newline`() {
        val buf = PieceTableBuffer("a\nb\n")
        assertEquals(3, buf.lineCount)
        assertEquals("a", buf.getLineText(0).toString())
        assertEquals("b", buf.getLineText(1).toString())
        assertEquals("", buf.getLineText(2).toString())
    }
}
