package com.androtext.app.ui.screens

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
import com.androtext.core.render.EditorConfig
import com.androtext.core.render.SoraEditorHost

@Composable
fun EditorHost(
    buffer: PieceTableBuffer,
    config: EditorConfig,
    fileVersion: Int,
    onContentChanged: () -> Unit,
    onHostReady: (SoraEditorHost) -> Unit,
    modifier: Modifier = Modifier,
) {
    var host by remember { mutableStateOf<SoraEditorHost?>(null) }
    var lastVersion by remember { mutableIntStateOf(0) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            SoraEditorHost(context).also { editorHost ->
                editorHost.createRenderer(buffer, config)
                editorHost.setContent(buffer.getText())
                editorHost.applySolarizedDarkTheme()
                host = editorHost
                onHostReady(editorHost)
            }
        },
        update = { editorHost ->
            if (fileVersion != lastVersion) {
                editorHost.setContent(buffer.getText())
                lastVersion = fileVersion
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
