package com.androtext.core.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import com.androtext.core.buffer.TextBuffer
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class SoraEditorHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    val editor: CodeEditor = CodeEditor(context, attrs, defStyleAttr)

    private var _renderer: SoraTextRenderer? = null
    val renderer: SoraTextRenderer? get() = _renderer

    private var _viewportManager: SoraViewportManager? = null
    val viewportManager: ViewportManager? get() = _viewportManager

    init {
        addView(editor, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        clipChildren = false
    }

    fun createRenderer(
        buffer: TextBuffer,
        config: EditorConfig = EditorConfig(),
    ): SoraTextRenderer {
        val r = SoraTextRenderer(buffer, editor)
        r.applyConfig(config)
        _renderer = r
        _viewportManager = SoraViewportManager(buffer, editor, config.overscanLines)
        return r
    }

    fun setContent(text: CharSequence) {
        editor.setText(text)
    }

    fun getContent(): String = editor.text.toString()

    fun applySolarizedDarkTheme() {
        val scheme = EditorColorScheme()
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, Color.parseColor("#002B36"))
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, Color.parseColor("#839496"))
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, Color.parseColor("#073642"))
        scheme.setColor(EditorColorScheme.LINE_NUMBER, Color.parseColor("#586E75"))
        scheme.setColor(EditorColorScheme.LINE_NUMBER_CURRENT, Color.parseColor("#93A1A1"))
        scheme.setColor(EditorColorScheme.CURRENT_LINE, Color.parseColor("#073642"))
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, Color.parseColor("#268BD2"))
        scheme.setColor(EditorColorScheme.SELECTION_HANDLE, Color.parseColor("#268BD2"))
        scheme.setColor(EditorColorScheme.SELECTED_TEXT_BACKGROUND, Color.parseColor("#268BD2"))
        scheme.setColor(EditorColorScheme.BLOCK_LINE, Color.parseColor("#586E75"))
        scheme.setColor(EditorColorScheme.BLOCK_LINE_CURRENT, Color.parseColor("#839496"))
        scheme.setColor(EditorColorScheme.SCROLL_BAR_THUMB, Color.parseColor("#586E75"))
        scheme.setColor(EditorColorScheme.SCROLL_BAR_TRACK, Color.parseColor("#073642"))
        scheme.setColor(EditorColorScheme.TEXT_SELECTED, Color.parseColor("#002B36"))
        scheme.setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_BACKGROUND, Color.parseColor("#073642"))
        scheme.setColor(EditorColorScheme.KEYWORD, Color.parseColor("#859900"))
        scheme.setColor(EditorColorScheme.COMMENT, Color.parseColor("#586E75"))
        scheme.setColor(EditorColorScheme.OPERATOR, Color.parseColor("#839496"))
        scheme.setColor(EditorColorScheme.LITERAL, Color.parseColor("#2AA198"))
        scheme.setColor(EditorColorScheme.FUNCTION_NAME, Color.parseColor("#268BD2"))
        scheme.setColor(EditorColorScheme.ANNOTATION, Color.parseColor("#B58900"))
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, Color.parseColor("#073642"))
        editor.colorScheme = scheme
    }

    override fun dispatchDraw(canvas: Canvas) {
        _viewportManager?.checkViewportChanged()
        super.dispatchDraw(canvas)
        _renderer?.render(canvas)
    }

    fun release() {
        _viewportManager = null
        _renderer = null
        editor.release()
    }

    fun search(query: String, caseSensitive: Boolean, regex: Boolean) {
        val options = io.github.rosemoe.sora.widget.EditorSearcher.SearchOptions(
            !caseSensitive,
            regex,
        )
        editor.searcher.search(query, options)
    }

    fun searchNext() {
        try {
            editor.searcher.gotoNext()
        } catch (_: Exception) {
        }
    }

    fun searchPrevious() {
        try {
            editor.searcher.gotoPrevious()
        } catch (_: Exception) {
        }
    }

    fun replaceCurrent(replacement: String) {
        try {
            editor.searcher.replaceCurrentMatch(replacement)
        } catch (_: Exception) {
        }
    }

    fun replaceAll(replacement: String) {
        try {
            editor.searcher.replaceAll(replacement)
        } catch (_: Exception) {
        }
    }

    fun stopSearch() {
        try {
            editor.searcher.stopSearch()
        } catch (_: Exception) {
        }
    }
}
