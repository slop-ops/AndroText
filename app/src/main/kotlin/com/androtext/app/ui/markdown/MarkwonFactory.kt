package com.androtext.app.ui.markdown

import android.content.Context
import android.graphics.Color
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.ImagesPlugin

object MarkwonFactory {

    private var instance: Markwon? = null
    private var lastBgColor: Int = -1
    private var lastFgColor: Int = -1
    private var lastAccentColor: Int = -1

    fun get(
        context: Context,
        backgroundColor: Int = Color.parseColor("#002B36"),
        foregroundColor: Int = Color.parseColor("#839496"),
        accentColor: Int = Color.parseColor("#268BD2"),
    ): Markwon {
        if (instance != null && lastBgColor == backgroundColor && lastFgColor == foregroundColor && lastAccentColor == accentColor) {
            return instance!!
        }
        lastBgColor = backgroundColor
        lastFgColor = foregroundColor
        lastAccentColor = accentColor

        instance = Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(ImagesPlugin.create())
            .build()
        return instance!!
    }

    fun clearCache() {
        instance = null
        lastBgColor = -1
        lastFgColor = -1
        lastAccentColor = -1
    }
}
