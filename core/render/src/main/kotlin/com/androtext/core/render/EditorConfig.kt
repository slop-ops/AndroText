package com.androtext.core.render

data class EditorConfig(
    val fontSize: Float = 14f,
    val fontFamily: String = "monospace",
    val showLineNumbers: Boolean = true,
    val lineSpacingExtra: Float = 0f,
    val lineSpacingMultiplier: Float = 1f,
    val tabWidth: Int = 4,
    val wordWrap: Boolean = false,
    val editable: Boolean = true,
    val highlightCurrentLine: Boolean = true,
    val cursorBlinkPeriod: Int = 500,
    val scalable: Boolean = true,
    val overscanLines: Int = 5,
)
