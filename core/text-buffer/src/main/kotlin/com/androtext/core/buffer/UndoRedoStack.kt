package com.androtext.core.buffer

interface UndoRedoStack {
    val canUndo: Boolean
    val canRedo: Boolean

    fun push(operation: EditOperation)

    fun undo(): EditOperation?

    fun redo(): EditOperation?

    fun beginTransaction(label: String? = null)

    fun endTransaction()

    fun clear()
}

sealed class EditOperation {
    abstract val startOffset: Int

    data class Insert(
        override val startOffset: Int,
        val text: CharSequence,
    ) : EditOperation()

    data class Delete(
        override val startOffset: Int,
        val deletedText: CharSequence,
    ) : EditOperation()

    data class Replace(
        override val startOffset: Int,
        val oldText: CharSequence,
        val newText: CharSequence,
    ) : EditOperation()

    data class Compound(
        val operations: List<EditOperation>,
        val label: String?,
    ) : EditOperation() {
        override val startOffset: Int
            get() = operations.firstOrNull()?.startOffset ?: 0
    }
}
