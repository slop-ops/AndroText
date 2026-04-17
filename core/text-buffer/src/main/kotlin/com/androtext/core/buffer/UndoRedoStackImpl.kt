package com.androtext.core.buffer

class UndoRedoStackImpl : UndoRedoStack {

    private val undoStack: MutableList<EditOperation> = mutableListOf()
    private val redoStack: MutableList<EditOperation> = mutableListOf()

    private var transactionDepth: Int = 0
    private var currentTransaction: MutableList<EditOperation>? = null
    private var currentTransactionLabel: String? = null

    override val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    override val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    override fun push(operation: EditOperation) {
        redoStack.clear()
        if (transactionDepth > 0) {
            currentTransaction?.add(operation)
        } else {
            undoStack.add(operation)
        }
    }

    override fun undo(): EditOperation? {
        if (!canUndo) return null
        val op = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(op)
        return op
    }

    override fun redo(): EditOperation? {
        if (!canRedo) return null
        val op = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(op)
        return op
    }

    override fun beginTransaction(label: String?) {
        transactionDepth++
        if (transactionDepth == 1) {
            currentTransaction = mutableListOf()
            currentTransactionLabel = label
        }
    }

    override fun endTransaction() {
        require(transactionDepth > 0) { "No active transaction" }
        transactionDepth--
        if (transactionDepth == 0) {
            val ops = currentTransaction
            if (ops != null && ops.isNotEmpty()) {
                push(EditOperation.Compound(ops.toList(), currentTransactionLabel))
            }
            currentTransaction = null
            currentTransactionLabel = null
        }
    }

    override fun clear() {
        undoStack.clear()
        redoStack.clear()
        transactionDepth = 0
        currentTransaction = null
        currentTransactionLabel = null
    }
}
