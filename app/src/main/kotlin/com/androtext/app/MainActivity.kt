package com.androtext.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.androtext.app.ui.navigation.Screen
import com.androtext.app.ui.screens.EditorHost
import com.androtext.app.ui.screens.EditorScreen
import com.androtext.app.ui.screens.MarkdownPreviewScreen
import com.androtext.app.ui.screens.OpenFileScreen
import com.androtext.app.ui.screens.SettingsScreen
import com.androtext.app.ui.theme.AndroTextTheme
import com.androtext.app.ui.viewmodel.EditorViewModel
import com.androtext.core.lang.LanguageRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    private val languageRegistry: LanguageRegistry = LanguageRegistry.getInstance()

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            loadFileFromUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initLanguageRegistry()
        viewModel.initializeTheme()
        handleIncomingIntent(intent)
        setContent {
            AndroTextTheme(themeColors = viewModel.currentComposeColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AndroTextApp(
                        viewModel = viewModel,
                        onFileSelected = { uri -> loadFileFromUri(uri) },
                        onOpenRecentFile = { uri -> loadFileFromUri(uri) },
                        onSave = { saveFile() },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_VIEW || action == Intent.ACTION_EDIT) {
            val uri = intent.data
            if (uri != null) {
                loadFileFromUri(uri)
            }
        }
    }

    private fun initLanguageRegistry() {
        languageRegistry.initialize(this)
        val scope = MainScope()
        scope.launch(Dispatchers.IO) {
            languageRegistry.loadAllThemes(assets)
            languageRegistry.registerAllLanguages()
        }
    }

    private fun getDisplayName(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
            ?: uri.path?.substringAfterLast('/') ?: "Untitled"
    }

    private fun loadFileFromUri(uri: Uri) {
        val scope = MainScope()
        scope.launch {
            viewModel.isLoading = true
            try {
                val content = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: return@withContext null
                }
                if (content != null) {
                    val fileName = getDisplayName(uri)
                    takePersistablePermission(uri)
                    viewModel.onFileOpened(uri, fileName, content)
                }
            } finally {
                viewModel.isLoading = false
            }
        }
    }

    private fun saveFile() {
        val uri = viewModel.currentFileUri ?: return
        val content = viewModel.getContent()
        val scope = MainScope()
        scope.launch {
            viewModel.isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri, "wt")?.use { os ->
                        os.write(content.toByteArray())
                    } ?: throw IllegalStateException("Cannot open output stream for $uri")
                }
                viewModel.onFileSaved()
            } finally {
                viewModel.isLoading = false
            }
        }
    }

    private fun takePersistablePermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        } catch (_: Exception) {
        }
    }

    private fun openFilePicker() {
        openFileLauncher.launch(arrayOf("*/*"))
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.isCtrlPressed) {
            val shift = event.isShiftPressed
            when (event.keyCode) {
                KeyEvent.KEYCODE_S -> {
                    if (!shift) { saveFile(); return true }
                }
                KeyEvent.KEYCODE_O -> {
                    if (!shift) { openFilePicker(); return true }
                }
                KeyEvent.KEYCODE_F -> {
                    if (!shift) { viewModel.toggleSearch(); return true }
                }
                KeyEvent.KEYCODE_H -> {
                    if (!shift) { viewModel.showReplace(); return true }
                }
                KeyEvent.KEYCODE_G -> {
                    if (shift) viewModel.searchPrevious() else viewModel.searchNext()
                    return true
                }
                KeyEvent.KEYCODE_W -> {
                    if (!shift) {
                        viewModel.activeTabId?.let { viewModel.closeTab(it) }
                        return true
                    }
                }
                KeyEvent.KEYCODE_EQUALS, KeyEvent.KEYCODE_NUMPAD_ADD -> {
                    viewModel.updateFontSize((viewModel.fontSize + 1).coerceAtMost(32f))
                    return true
                }
                KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> {
                    viewModel.updateFontSize((viewModel.fontSize - 1).coerceAtLeast(8f))
                    return true
                }
                KeyEvent.KEYCODE_0 -> {
                    if (!shift) { viewModel.updateFontSize(14f); return true }
                }
            }
        }
        if (event.action == KeyEvent.ACTION_DOWN
            && event.keyCode == KeyEvent.KEYCODE_ESCAPE
            && viewModel.isSearchOpen
        ) {
            viewModel.closeSearch()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun AndroTextApp(
    viewModel: EditorViewModel,
    onFileSelected: (Uri) -> Unit,
    onOpenRecentFile: (Uri) -> Unit,
    onSave: () -> Unit,
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Editor) }

    when (currentScreen) {
        is Screen.Editor -> EditorScreen(
            viewModel = viewModel,
            onOpenFile = { currentScreen = Screen.OpenFile },
            onOpenRecentFile = onOpenRecentFile,
            onSettings = { currentScreen = Screen.Settings },
            onSave = onSave,
            onTabSelected = { viewModel.switchToTab(it) },
            onTabClosed = { viewModel.closeTab(it) },
            editorContent = {
                val buffer = viewModel.activeBuffer
                if (buffer != null) {
                    EditorHost(
                        buffer = buffer,
                        config = viewModel.editorConfig,
                        fileVersion = viewModel.fileVersion,
                        fileName = viewModel.currentFileName,
                        themeId = viewModel.currentThemeId,
                        onContentChanged = { viewModel.onContentChanged() },
                        onHostReady = { host ->
                            viewModel.editorContentProvider =
                                { host.getContent() }
                            viewModel.doSearch = { q, cs, rx ->
                                host.search(q, cs, rx)
                            }
                            viewModel.doSearchNext = { host.searchNext() }
                            viewModel.doSearchPrev = { host.searchPrevious() }
                            viewModel.doReplaceCurrent = { r ->
                                host.replaceCurrent(r)
                            }
                            viewModel.doReplaceAll = { r ->
                                host.replaceAll(r)
                            }
                            viewModel.doStopSearch = { host.stopSearch() }
                        },
                    )
                }
            },
            previewContent = {
                val content = viewModel.getContent()
                val composeColors = viewModel.currentComposeColors
                val bgColor = composeColors?.background?.let {
                    ((255 shl 24) or
                        ((it.red.coerceIn(0f, 1f) * 255).toInt() shl 16) or
                        ((it.green.coerceIn(0f, 1f) * 255).toInt() shl 8) or
                        (it.blue.coerceIn(0f, 1f) * 255).toInt())
                } ?: android.graphics.Color.parseColor("#002B36")
                val fgColor = composeColors?.onBackground?.let {
                    ((255 shl 24) or
                        ((it.red.coerceIn(0f, 1f) * 255).toInt() shl 16) or
                        ((it.green.coerceIn(0f, 1f) * 255).toInt() shl 8) or
                        (it.blue.coerceIn(0f, 1f) * 255).toInt())
                } ?: android.graphics.Color.parseColor("#839496")
                val accentColor = composeColors?.primary?.let {
                    ((255 shl 24) or
                        ((it.red.coerceIn(0f, 1f) * 255).toInt() shl 16) or
                        ((it.green.coerceIn(0f, 1f) * 255).toInt() shl 8) or
                        (it.blue.coerceIn(0f, 1f) * 255).toInt())
                } ?: android.graphics.Color.parseColor("#268BD2")
                MarkdownPreviewScreen(
                    markdownText = content,
                    backgroundColor = bgColor,
                    foregroundColor = fgColor,
                    accentColor = accentColor,
                    fontSize = viewModel.fontSize,
                )
            },
        )

        is Screen.OpenFile -> OpenFileScreen(
            onBack = { currentScreen = Screen.Editor },
            onFileSelected = { uri ->
                onFileSelected(uri)
                currentScreen = Screen.Editor
            },
            recentFiles = viewModel.recentFiles,
            onOpenRecentFile = { uri ->
                onOpenRecentFile(uri)
                currentScreen = Screen.Editor
            },
            onRemoveRecentFile = { uriString ->
                viewModel.removeRecentFile(uriString)
            },
        )

        is Screen.Settings -> SettingsScreen(
            viewModel = viewModel,
            onBack = { currentScreen = Screen.Editor },
        )
    }
}
