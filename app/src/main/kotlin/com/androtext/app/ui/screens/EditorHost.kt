package com.androtext.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.androtext.core.buffer.PieceTableBuffer
import com.androtext.core.lang.LanguageRegistry
import com.androtext.core.lang.TextMateLanguageService
import com.androtext.core.render.EditorConfig
import com.androtext.core.render.SoraEditorHost
import com.androtext.core.render.theme.ThemeRegistry

@Composable
fun EditorHost(
    buffer: PieceTableBuffer,
    config: EditorConfig,
    fileVersion: Int,
    fileName: String?,
    themeId: String,
    onContentChanged: () -> Unit,
    onHostReady: (SoraEditorHost) -> Unit,
    modifier: Modifier = Modifier,
) {
    var host by remember { mutableStateOf<SoraEditorHost?>(null) }
    var lastVersion by remember { mutableIntStateOf(0) }
    var lastAppliedFile by remember { mutableStateOf<String?>(null) }
    var lastAppliedTheme by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            SoraEditorHost(context).also { editorHost ->
                editorHost.createRenderer(buffer, config)
                editorHost.setContent(buffer.getText())
                applyTheme(context, editorHost, themeId)
                lastAppliedTheme = themeId
                applyLanguage(editorHost, fileName)
                lastAppliedFile = fileName
                host = editorHost
                onHostReady(editorHost)
            }
        },
        update = { editorHost ->
            if (fileVersion != lastVersion) {
                editorHost.createRenderer(buffer, config)
                editorHost.setContent(buffer.getText())
                if (fileName != lastAppliedFile) {
                    applyLanguage(editorHost, fileName)
                    lastAppliedFile = fileName
                }
                lastVersion = fileVersion
            }
            if (themeId != lastAppliedTheme) {
                applyTheme(editorHost.context, editorHost, themeId)
                lastAppliedTheme = themeId
            }
            val renderer = editorHost.renderer
            if (renderer != null) {
                renderer.applyConfig(config)
            }
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            host?.release()
            host = null
        }
    }
}

private fun applyTheme(context: Context, editorHost: SoraEditorHost, themeId: String) {
    val registry = ThemeRegistry.getInstance()
    val scheme = registry.getEditorColorScheme(context.assets, themeId)
    if (scheme != null) {
        editorHost.applyTheme(scheme)
    }
}

private fun applyLanguage(editorHost: SoraEditorHost, fileName: String?) {
    if (fileName == null) return
    val registry = LanguageRegistry.getInstance()
    val languageService = registry.getLanguageForFile(fileName)
    if (languageService is TextMateLanguageService) {
        try {
            val language = languageService.createSoraLanguage()
            editorHost.setLanguage(language)
        } catch (_: Exception) {
            editorHost.setLanguage(null)
        }
    } else {
        editorHost.setLanguage(null)
    }
}
