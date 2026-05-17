package com.androtext.app.ui.screens

import android.graphics.Color
import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.androtext.app.ui.markdown.MarkwonFactory

@Composable
fun MarkdownPreviewScreen(
    markdownText: String,
    backgroundColor: Int,
    foregroundColor: Int,
    accentColor: Int,
    fontSize: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markwon = remember(backgroundColor, foregroundColor, accentColor) {
        MarkwonFactory.get(context, backgroundColor, foregroundColor, accentColor)
    }

    AndroidView(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = LinkMovementMethod.getInstance()
                textSize = fontSize
                setTextColor(foregroundColor)
                setLinkTextColor(accentColor)
                setBackgroundColor(backgroundColor)
                setTypeface(Typeface.create("sans-serif", Typeface.NORMAL))
                setLineSpacing(4f, 1.1f)
            }
        },
        update = { textView ->
            textView.setBackgroundColor(backgroundColor)
            textView.textSize = fontSize
            markwon.setMarkdown(textView, markdownText)
            textView.setTextColor(foregroundColor)
            textView.setLinkTextColor(accentColor)
        },
    )
}
