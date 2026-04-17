package com.androtext.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.androtext.core.buffer.PieceTableBuffer
import com.androtext.core.render.EditorConfig
import org.json.JSONArray
import org.json.JSONObject

data class RecentFile(
    val uriString: String,
    val displayName: String,
    val lastOpened: Long,
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "androtext"
        private const val KEY_RECENT_FILES = "recent_files"
        private const val MAX_RECENT_FILES = 20
    }

    var currentFileName by mutableStateOf<String?>(null)
        private set

    var currentFileUri by mutableStateOf<Uri?>(null)
        private set

    var isModified by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        internal set

    var fileVersion by mutableIntStateOf(0)
        private set

    var editorConfig by mutableStateOf(EditorConfig())
        private set

    var fontSize by mutableFloatStateOf(14f)
        private set

    var showLineNumbers by mutableStateOf(true)
        private set

    var tabWidth by mutableIntStateOf(4)
        private set

    var wordWrap by mutableStateOf(false)
        private set

    var highlightCurrentLine by mutableStateOf(true)
        private set

    var recentFiles by mutableStateOf<List<RecentFile>>(emptyList())
        private set

    var editorContentProvider: (() -> String)? = null

    val buffer = PieceTableBuffer()

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        loadRecentFiles()
    }

    fun onFileOpened(uri: Uri, fileName: String, content: String) {
        currentFileName = fileName
        currentFileUri = uri
        buffer.replace(0, buffer.length, content)
        isModified = false
        fileVersion++
        addRecentFile(uri, fileName)
    }

    fun onFileSaved() {
        isModified = false
    }

    fun onContentChanged() {
        isModified = true
    }

    fun getContent(): String = editorContentProvider?.invoke() ?: buffer.getText().toString()

    fun addRecentFile(uri: Uri, displayName: String) {
        val uriString = uri.toString()
        val current = recentFiles.toMutableList()
        current.removeAll { it.uriString == uriString }
        current.add(0, RecentFile(uriString, displayName, System.currentTimeMillis()))
        recentFiles = if (current.size > MAX_RECENT_FILES) {
            current.take(MAX_RECENT_FILES)
        } else {
            current
        }
        saveRecentFiles()
    }

    fun removeRecentFile(uriString: String) {
        recentFiles = recentFiles.filter { it.uriString != uriString }
        saveRecentFiles()
    }

    private fun loadRecentFiles() {
        val json = prefs.getString(KEY_RECENT_FILES, null) ?: return
        try {
            val array = JSONArray(json)
            val files = mutableListOf<RecentFile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                files.add(
                    RecentFile(
                        uriString = obj.getString("uri"),
                        displayName = obj.getString("name"),
                        lastOpened = obj.getLong("time"),
                    )
                )
            }
            recentFiles = files
        } catch (_: Exception) {
        }
    }

    private fun saveRecentFiles() {
        val array = JSONArray()
        for (file in recentFiles) {
            val obj = JSONObject()
            obj.put("uri", file.uriString)
            obj.put("name", file.displayName)
            obj.put("time", file.lastOpened)
            array.put(obj)
        }
        prefs.edit().putString(KEY_RECENT_FILES, array.toString()).apply()
    }

    fun updateFontSize(size: Float) {
        fontSize = size
        editorConfig = editorConfig.copy(fontSize = size)
    }

    fun updateShowLineNumbers(show: Boolean) {
        showLineNumbers = show
        editorConfig = editorConfig.copy(showLineNumbers = show)
    }

    fun updateTabWidth(width: Int) {
        tabWidth = width
        editorConfig = editorConfig.copy(tabWidth = width)
    }

    fun updateWordWrap(wrap: Boolean) {
        wordWrap = wrap
        editorConfig = editorConfig.copy(wordWrap = wrap)
    }

    fun updateHighlightCurrentLine(highlight: Boolean) {
        highlightCurrentLine = highlight
        editorConfig = editorConfig.copy(highlightCurrentLine = highlight)
    }
}
