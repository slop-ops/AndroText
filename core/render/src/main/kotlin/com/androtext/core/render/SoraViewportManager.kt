package com.androtext.core.render

import com.androtext.core.buffer.TextBuffer
import io.github.rosemoe.sora.widget.CodeEditor

class SoraViewportManager(
    private val buffer: TextBuffer,
    private val editor: CodeEditor,
    private val defaultOverscan: Int = 5,
) : ViewportManager {

    private val listeners = mutableListOf<ViewportListener>()
    private var lastFirstVisible = -1
    private var lastLastVisible = -1

    override val firstVisibleLine: Int
        get() = editor.firstVisibleLine

    override val lastVisibleLine: Int
        get() = editor.lastVisibleLine

    override val visibleLineCount: Int
        get() = lastVisibleLine - firstVisibleLine + 1

    override val totalLineCount: Int
        get() = buffer.lineCount

    override val scrollFraction: Float
        get() {
            val total = totalLineCount
            val visible = visibleLineCount
            if (total <= visible) return 0f
            return firstVisibleLine.toFloat() / (total - visible).coerceAtLeast(1)
        }

    override fun getVisibleRange(overscan: Int): IntRange {
        val effectiveOverscan = if (overscan < 0) defaultOverscan else overscan
        val first = maxOf(0, firstVisibleLine - effectiveOverscan)
        val last = minOf(totalLineCount - 1, lastVisibleLine + effectiveOverscan)
        return first..last
    }

    override fun jumpToLine(line: Int) {
        val clamped = line.coerceIn(0, maxOf(0, totalLineCount - 1))
        editor.setSelection(clamped, 0)
    }

    override fun jumpToFraction(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        val target = (clamped * maxOf(0, totalLineCount - 1)).toInt()
        jumpToLine(target)
    }

    override fun ensureLineVisible(line: Int) {
        if (line < 0 || line >= totalLineCount) return
        val first = firstVisibleLine
        val last = lastVisibleLine
        if (line < first || line > last) {
            editor.setSelection(line, 0)
        }
    }

    override fun addViewportListener(listener: ViewportListener) {
        listeners.add(listener)
    }

    override fun removeViewportListener(listener: ViewportListener) {
        listeners.remove(listener)
    }

    fun checkViewportChanged() {
        val first = firstVisibleLine
        val last = lastVisibleLine
        if (first != lastFirstVisible || last != lastLastVisible) {
            lastFirstVisible = first
            lastLastVisible = last
            for (listener in listeners.toList()) {
                listener.onViewportChanged(first, last)
            }
        }
    }
}
