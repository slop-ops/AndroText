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

    var tabs by mutableStateOf<List<EditorTab>>(emptyList())
        private set

    var activeTabId by mutableStateOf<String?>(null)
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

    var isSearchOpen by mutableStateOf(false)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var replaceQuery by mutableStateOf("")
        private set
    var isReplaceMode by mutableStateOf(false)
        private set
    var matchCount by mutableIntStateOf(0)
        private set
    var currentMatch by mutableIntStateOf(0)
        private set
    var caseSensitive by mutableStateOf(false)
        private set
    var regexSearch by mutableStateOf(false)
        private set
    var requestSearchFocus by mutableStateOf(false)
        internal set

    var doSearch: ((String, Boolean, Boolean) -> Unit)? = null
    var doSearchNext: (() -> Unit)? = null
    var doSearchPrev: (() -> Unit)? = null
    var doReplaceCurrent: ((String) -> Unit)? = null
    var doReplaceAll: ((String) -> Unit)? = null
    var doStopSearch: (() -> Unit)? = null

    val activeTab: EditorTab? get() = tabs.find { it.id == activeTabId }

    val activeBuffer: PieceTableBuffer? get() = activeTab?.buffer

    val currentFileName: String? get() = activeTab?.fileName

    val currentFileUri: Uri? get() = activeTab?.uri

    val isModified: Boolean get() = activeTab?.isModified ?: false

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        loadRecentFiles()
    }

    fun onFileOpened(uri: Uri, fileName: String, content: String) {
        val existing = tabs.find { it.uri == uri }
        if (existing != null) {
            switchToTab(existing.id)
            return
        }

        saveCurrentEditorContent()

        val tab = EditorTab(uri = uri, fileName = fileName)
        tab.buffer.replace(0, tab.buffer.length, content)
        tabs = tabs + tab
        activeTabId = tab.id
        fileVersion++
        addRecentFile(uri, fileName)
    }

    fun switchToTab(tabId: String) {
        if (tabId == activeTabId) return
        saveCurrentEditorContent()
        if (isSearchOpen) closeSearch()
        activeTabId = tabId
        fileVersion++
    }

    fun closeTab(tabId: String) {
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (idx < 0) return

        val wasActive = tabId == activeTabId
        tabs = tabs.filter { it.id != tabId }
        if (isSearchOpen) closeSearch()

        if (wasActive) {
            if (tabs.isNotEmpty()) {
                val newIdx = minOf(idx, tabs.size - 1)
                activeTabId = tabs[newIdx].id
            } else {
                activeTabId = null
            }
            fileVersion++
        }
    }

    fun onFileSaved() {
        activeTab?.isModified = false
    }

    fun onContentChanged() {
        activeTab?.isModified = true
    }

    fun getContent(): String = editorContentProvider?.invoke()
        ?: activeTab?.buffer?.getText()?.toString() ?: ""

    private fun saveCurrentEditorContent() {
        val content = editorContentProvider?.invoke() ?: return
        val tab = activeTab ?: return
        tab.buffer.replace(0, tab.buffer.length, content)
    }

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

    fun toggleSearch() {
        if (isSearchOpen) {
            closeSearch()
        } else {
            isSearchOpen = true
            isReplaceMode = false
            requestSearchFocus = true
        }
    }

    fun showReplace() {
        isSearchOpen = true
        isReplaceMode = true
        requestSearchFocus = true
    }

    fun closeSearch() {
        isSearchOpen = false
        searchQuery = ""
        replaceQuery = ""
        isReplaceMode = false
        matchCount = 0
        currentMatch = 0
        doStopSearch?.invoke()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        performSearch()
    }

    fun updateReplaceQuery(query: String) {
        replaceQuery = query
    }

    fun toggleReplaceMode() {
        isReplaceMode = !isReplaceMode
    }

    fun toggleCaseSensitive() {
        caseSensitive = !caseSensitive
        performSearch()
    }

    fun toggleRegex() {
        regexSearch = !regexSearch
        performSearch()
    }

    fun searchNext() {
        if (matchCount == 0) return
        doSearchNext?.invoke()
        currentMatch = if (currentMatch >= matchCount) 1 else currentMatch + 1
    }

    fun searchPrevious() {
        if (matchCount == 0) return
        doSearchPrev?.invoke()
        currentMatch = if (currentMatch <= 1) matchCount else currentMatch - 1
    }

    fun performReplace() {
        doReplaceCurrent?.invoke(replaceQuery)
        onContentChanged()
        performSearch()
    }

    fun performReplaceAll() {
        doReplaceAll?.invoke(replaceQuery)
        onContentChanged()
        performSearch()
    }

    fun clearSearchFocusRequest() {
        requestSearchFocus = false
    }

    private fun performSearch() {
        if (searchQuery.isEmpty()) {
            matchCount = 0
            currentMatch = 0
            doStopSearch?.invoke()
            return
        }
        try {
            doSearch?.invoke(searchQuery, caseSensitive, regexSearch)
            val content = editorContentProvider?.invoke() ?: ""
            matchCount = countMatches(content, searchQuery, caseSensitive, regexSearch)
            currentMatch = if (matchCount > 0) 1 else 0
        } catch (_: Exception) {
            matchCount = 0
            currentMatch = 0
        }
    }

    private fun countMatches(text: String, query: String, cs: Boolean, isRegex: Boolean): Int {
        if (query.isEmpty() || text.isEmpty()) return 0
        return try {
            if (isRegex) {
                val options = if (cs) setOf<RegexOption>() else setOf(RegexOption.IGNORE_CASE)
                Regex(query, options).findAll(text).count()
            } else {
                val s = if (cs) text else text.lowercase()
                val q = if (cs) query else query.lowercase()
                var count = 0
                var idx = 0
                while (true) {
                    idx = s.indexOf(q, idx)
                    if (idx == -1) break
                    count++
                    idx += q.length
                }
                count
            }
        } catch (_: Exception) {
            0
        }
    }
}
