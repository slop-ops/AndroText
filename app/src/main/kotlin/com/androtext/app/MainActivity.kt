package com.androtext.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.androtext.app.ui.screens.OpenFileScreen
import com.androtext.app.ui.screens.SettingsScreen
import com.androtext.app.ui.screens.WelcomeScreen
import com.androtext.app.ui.theme.AndroTextTheme
import com.androtext.app.ui.viewmodel.EditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroTextTheme {
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
                        onContentChanged = { viewModel.onContentChanged() },
                        onHostReady = { host ->
                            viewModel.editorContentProvider =
                                { host.getContent() }
                        },
                    )
                }
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
