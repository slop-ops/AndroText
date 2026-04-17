package com.androtext.core.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.androtext.core.buffer.TextBuffer
import io.github.rosemoe.sora.widget.CodeEditor

class SoraTextRenderer(
    override val buffer: TextBuffer,
    private val editor: CodeEditor,
) : TextRenderer {

    private var _config: EditorConfig = EditorConfig()
    val config: EditorConfig get() = _config

    private var _decorationProvider: DecorationProvider? = null

    private val decorationRect = RectF()
    private val decorationPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setDecorationProvider(provider: DecorationProvider?) {
        _decorationProvider = provider
    }

    fun applyConfig(config: EditorConfig) {
        _config = config
        editor.setTextSize(config.fontSize)
        editor.setTypefaceText(Typeface.create(config.fontFamily, Typeface.NORMAL))
        editor.isLineNumberEnabled = config.showLineNumbers
        editor.setLineSpacing(config.lineSpacingExtra, config.lineSpacingMultiplier)
        editor.tabWidth = config.tabWidth
        editor.setWordwrap(config.wordWrap)
        editor.setEditable(config.editable)
        editor.setHighlightCurrentLine(config.highlightCurrentLine)
        editor.setCursorBlinkPeriod(config.cursorBlinkPeriod)
        editor.setScalable(config.scalable)
        editor.invalidate()
    }

    override fun render(canvas: Any) {
        if (canvas !is Canvas) return
        val provider = _decorationProvider ?: return
        for (decoration in provider.decorations) {
            drawDecoration(canvas, decoration)
        }
    }

    override fun setFontSize(size: Float) {
        editor.setTextSize(size)
    }

    override fun setFontFamily(family: String) {
        editor.setTypefaceText(Typeface.create(family, Typeface.NORMAL))
    }

    override fun setShowLineNumbers(show: Boolean) {
        editor.isLineNumberEnabled = show
    }

    override fun setLineSpacing(extra: Float, multiplier: Float) {
        editor.setLineSpacing(extra, multiplier)
    }

    override fun invalidate() {
        editor.invalidate()
    }

    private fun drawDecoration(canvas: Canvas, decoration: Decoration) {
        if (decoration.color == null) return

        val startLine = buffer.offsetToLine(decoration.startOffset)
        val endLine = buffer.offsetToLine(decoration.endOffset)

        if (startLine < 0 || endLine < 0) return

        val firstVisible = editor.firstVisibleLine
        val lastVisible = editor.lastVisibleLine

        if (endLine < firstVisible || startLine > lastVisible) return

        val effectiveStartLine = maxOf(startLine, firstVisible)
        val effectiveEndLine = minOf(endLine, lastVisible)

        val lineHeight = editor.rowHeight.toFloat()

        decorationPaint.color = decoration.color.toInt()
        decorationPaint.alpha = when (decoration.type) {
            DecorationType.SELECTION -> 80
            DecorationType.SEARCH_MATCH -> 100
            DecorationType.ERROR -> 60
            DecorationType.WARNING -> 60
            else -> 80
        }

        for (line in effectiveStartLine..effectiveEndLine) {
            val charStart = if (line == startLine) {
                decoration.startOffset - buffer.getLineStart(line)
            } else {
                0
            }

            val lineEnd = buffer.getLineEnd(line)
            val charEnd = if (line == endLine) {
                decoration.endOffset - buffer.getLineStart(line)
            } else {
                if (lineEnd == -1) buffer.length - buffer.getLineStart(line)
                else lineEnd - buffer.getLineStart(line)
            }

            val text = buffer.getLineText(line)
            val textPaint = editor.textPaint

            val startX = textPaint.measureText(text, 0, minOf(charStart, text.length))
            val endX = textPaint.measureText(text, 0, minOf(charEnd, text.length))

            val topOffset = (line - firstVisible) * lineHeight

            decorationRect.set(startX, topOffset, endX, topOffset + lineHeight)
            canvas.drawRect(decorationRect, decorationPaint)
        }
    }
}
