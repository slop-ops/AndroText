package com.androtext.core.buffer

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class FileLoadResult(
    val buffer: PieceTableBuffer,
    val encoding: Charset,
    val fileSize: Long,
)

object FileLoader {

    private const val CHUNK_SIZE = 64 * 1024

    suspend fun load(
        path: Path,
        encoding: Charset = StandardCharsets.UTF_8,
        onCancel: (() -> Unit)? = null,
    ): FileLoadResult = withContext(Dispatchers.IO) {
        val fileSize = Files.size(path)
        val decoder = encoding.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)

        val channel = FileChannel.open(path, StandardOpenOption.READ)
        try {
            val totalChars = decoder.maxCharsPerByte().let { (fileSize * it).toInt() }
            val text = StringBuilder(if (totalChars > 0) totalChars else 8192)

            var byteBuffer = ByteBuffer.allocateDirect(CHUNK_SIZE)
            var charBuffer = decoder.decode(byteBuffer)

            while (true) {
                coroutineContext.ensureActive()
                byteBuffer.clear()
                val bytesRead = channel.read(byteBuffer)
                if (bytesRead == -1 || bytesRead == 0) break
                byteBuffer.flip()
                charBuffer = decoder.decode(byteBuffer)
                text.append(charBuffer)
            }

            val detectedEncoding = detectEncoding(path, encoding)
            FileLoadResult(
                buffer = PieceTableBuffer(text.toString()),
                encoding = detectedEncoding,
                fileSize = fileSize,
            )
        } finally {
            channel.close()
        }
    }

    private fun detectEncoding(path: Path, default: Charset): Charset {
        try {
            FileChannel.open(path, StandardOpenOption.READ).use { channel ->
                val header = ByteBuffer.allocate(4)
                val read = channel.read(header)
                if (read >= 3) {
                    header.flip()
                    val b0 = header.get(0).toInt() and 0xFF
                    val b1 = header.get(1).toInt() and 0xFF
                    val b2 = header.get(2).toInt() and 0xFF

                    if (b0 == 0xEF && b1 == 0xBB && b2 == 0xBF) {
                        return StandardCharsets.UTF_8
                    }
                    if (b0 == 0xFF && b1 == 0xFE) {
                        return StandardCharsets.UTF_16LE
                    }
                    if (b0 == 0xFE && b1 == 0xFF) {
                        return StandardCharsets.UTF_16BE
                    }
                }
            }
        } catch (_: IOException) {
        }
        return default
    }
}

object FileWriter {

    private const val CHUNK_SIZE = 64 * 1024

    suspend fun write(
        path: Path,
        buffer: TextBuffer,
        encoding: Charset = StandardCharsets.UTF_8,
    ): Unit = withContext(Dispatchers.IO) {
        val tempPath = path.resolveSibling(path.fileName.toString() + ".androtext.tmp")

        FileChannel.open(
            tempPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
        ).use { channel ->
            val encoder = encoding.newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)

            val text = buffer.getText()
            var offset = 0
            while (offset < text.length) {
                coroutineContext.ensureActive()
                val end = minOf(offset + CHUNK_SIZE, text.length)
                val chunk = text.subSequence(offset, end)
                val byteBuffer = encoder.encode(java.nio.CharBuffer.wrap(chunk))
                while (byteBuffer.hasRemaining()) {
                    channel.write(byteBuffer)
                }
                offset = end
            }
            channel.force(true)
        }

        try {
            Files.move(tempPath, path, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(tempPath, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
