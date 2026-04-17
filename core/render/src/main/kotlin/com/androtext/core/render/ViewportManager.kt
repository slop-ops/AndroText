package com.androtext.core.render

interface ViewportManager {
    val firstVisibleLine: Int
    val lastVisibleLine: Int
    val visibleLineCount: Int
    val totalLineCount: Int
    val scrollFraction: Float

    fun getVisibleRange(overscan: Int = 0): IntRange

    fun jumpToLine(line: Int)

    fun jumpToFraction(fraction: Float)

    fun ensureLineVisible(line: Int)

    fun addViewportListener(listener: ViewportListener)

    fun removeViewportListener(listener: ViewportListener)
}

fun interface ViewportListener {
    fun onViewportChanged(firstLine: Int, lastLine: Int)
}
