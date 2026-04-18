package com.androtext.app.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.androtext.core.buffer.PieceTableBuffer
import java.util.UUID

class EditorTab(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val fileName: String,
    val buffer: PieceTableBuffer = PieceTableBuffer(),
) {
    var isModified by mutableStateOf(false)
}
