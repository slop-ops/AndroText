package com.androtext.core.render

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.androtext.core.buffer.TextBuffer
import io.github.rosemoe.sora.lang.Language
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

    fun setLanguage(language: Language?) {
        editor.setEditorLanguage(language)
    }

    fun applyTheme(scheme: EditorColorScheme) {
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
