package com.androtext.core.buffer

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class FileIOTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `load reads file content`() = runTest {
        val file = tempDir.resolve("test.txt")
        Files.writeString(file, "hello world")
        val result = FileLoader.load(file)
        assertEquals("hello world", result.buffer.getText().toString())
        assertEquals(StandardCharsets.UTF_8, result.encoding)
        assertEquals(11L, result.fileSize)
    }

    @Test
    fun `load handles empty file`() = runTest {
        val file = tempDir.resolve("empty.txt")
        Files.writeString(file, "")
        val result = FileLoader.load(file)
        assertEquals("", result.buffer.getText().toString())
        assertEquals(0L, result.fileSize)
    }

    @Test
    fun `load handles multiline content`() = runTest {
        val file = tempDir.resolve("multi.txt")
        Files.writeString(file, "line1\nline2\nline3")
        val result = FileLoader.load(file)
        assertEquals("line1\nline2\nline3", result.buffer.getText().toString())
        assertEquals(3, result.buffer.lineCount)
    }

    @Test
    fun `load detects UTF-8 BOM`() = runTest {
        val file = tempDir.resolve("bom.txt")
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "hello".toByteArray()
        Files.write(file, bytes)
        val result = FileLoader.load(file)
        assertEquals(StandardCharsets.UTF_8, result.encoding)
    }

    @Test
    fun `write saves buffer content`() = runTest {
        val file = tempDir.resolve("out.txt")
        val buf = PieceTableBuffer("hello world")
        FileWriter.write(file, buf)
        assertEquals("hello world", Files.readString(file))
    }

    @Test
    fun `write then load roundtrip`() = runTest {
        val file = tempDir.resolve("roundtrip.txt")
        val original = "Hello, AndroText!\nLine 2\nLine 3"
        val buf = PieceTableBuffer(original)
        FileWriter.write(file, buf)
        val result = FileLoader.load(file)
        assertEquals(original, result.buffer.getText().toString())
    }

    @Test
    fun `write overwrites existing file`() = runTest {
        val file = tempDir.resolve("overwrite.txt")
        Files.writeString(file, "old content")
        val buf = PieceTableBuffer("new content")
        FileWriter.write(file, buf)
        assertEquals("new content", Files.readString(file))
    }

    @Test
    fun `write handles empty buffer`() = runTest {
        val file = tempDir.resolve("empty.txt")
        val buf = PieceTableBuffer("")
        FileWriter.write(file, buf)
        assertEquals("", Files.readString(file))
    }

    @Test
    fun `load large file`() = runTest {
        val file = tempDir.resolve("large.txt")
        val sb = StringBuilder()
        for (i in 1..10000) {
            sb.append("Line $i\n")
        }
        Files.writeString(file, sb.toString())
        val result = FileLoader.load(file)
        assertEquals(sb.toString(), result.buffer.getText().toString())
        assertEquals(10001, result.buffer.lineCount)
    }
}
