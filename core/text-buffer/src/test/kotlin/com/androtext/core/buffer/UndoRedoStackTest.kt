package com.androtext.core.buffer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UndoRedoStackTest {

    @Test
    fun `empty stack cannot undo or redo`() {
        val stack = UndoRedoStackImpl()
        assertFalse(stack.canUndo)
        assertFalse(stack.canRedo)
        assertNull(stack.undo())
        assertNull(stack.redo())
    }

    @Test
    fun `push then undo`() {
        val stack = UndoRedoStackImpl()
        val op = EditOperation.Insert(0, "hello")
        stack.push(op)
        assertTrue(stack.canUndo)
        val undone = stack.undo()
        assertEquals(op, undone)
        assertFalse(stack.canUndo)
        assertTrue(stack.canRedo)
    }

    @Test
    fun `undo then redo restores state`() {
        val stack = UndoRedoStackImpl()
        stack.push(EditOperation.Insert(0, "hello"))
        stack.undo()
        assertTrue(stack.canRedo)
        val redone = stack.redo()
        assertNotNull(redone)
        assertFalse(stack.canRedo)
        assertTrue(stack.canUndo)
    }

    @Test
    fun `push clears redo stack`() {
        val stack = UndoRedoStackImpl()
        stack.push(EditOperation.Insert(0, "a"))
        stack.push(EditOperation.Insert(1, "b"))
        stack.undo()
        assertTrue(stack.canRedo)
        stack.push(EditOperation.Insert(1, "c"))
        assertFalse(stack.canRedo)
    }

    @Test
    fun `multiple undos in order`() {
        val stack = UndoRedoStackImpl()
        stack.push(EditOperation.Insert(0, "a"))
        stack.push(EditOperation.Insert(1, "b"))
        stack.push(EditOperation.Insert(2, "c"))

        val op1 = stack.undo()
        assertTrue(op1 is EditOperation.Insert)
        assertEquals("c", (op1 as EditOperation.Insert).text.toString())

        val op2 = stack.undo()
        assertTrue(op2 is EditOperation.Insert)
        assertEquals("b", (op2 as EditOperation.Insert).text.toString())
    }

    @Test
    fun `delete operation undo`() {
        val stack = UndoRedoStackImpl()
        stack.push(EditOperation.Delete(0, "hello"))
        val op = stack.undo()
        assertTrue(op is EditOperation.Delete)
        assertEquals("hello", (op as EditOperation.Delete).deletedText.toString())
    }

    @Test
    fun `replace operation undo`() {
        val stack = UndoRedoStackImpl()
        stack.push(EditOperation.Replace(0, "old", "new"))
        val op = stack.undo()
        assertTrue(op is EditOperation.Replace)
        val replace = op as EditOperation.Replace
        assertEquals("old", replace.oldText.toString())
        assertEquals("new", replace.newText.toString())
    }

    @Test
    fun `transaction groups operations`() {
        val stack = UndoRedoStackImpl()
        stack.beginTransaction("type word")
        stack.push(EditOperation.Insert(0, "h"))
        stack.push(EditOperation.Insert(1, "i"))
        stack.endTransaction()

        assertTrue(stack.canUndo)
        val op = stack.undo()
        assertTrue(op is EditOperation.Compound)
        val compound = op as EditOperation.Compound
        assertEquals("type word", compound.label)
        assertEquals(2, compound.operations.size)
    }

    @Test
    fun `transaction undo and redo`() {
        val stack = UndoRedoStackImpl()
        stack.beginTransaction()
        stack.push(EditOperation.Insert(0, "a"))
        stack.push(EditOperation.Insert(1, "b"))
        stack.endTransaction()

        val undone = stack.undo()
        assertTrue(stack.canRedo)
        val redone = stack.redo()
        assertTrue(redone is EditOperation.Compound)
        assertEquals(2, (redone as EditOperation.Compound).operations.size)
    }

    @Test
    fun `nested transactions`() {
        val stack = UndoRedoStackImpl()
        stack.beginTransaction("outer")
        stack.push(EditOperation.Insert(0, "a"))
        stack.beginTransaction("inner")
        stack.push(EditOperation.Insert(1, "b"))
        stack.endTransaction()
        stack.push(EditOperation.Insert(2, "c"))
        stack.endTransaction()

        val op = stack.undo()
        assertTrue(op is EditOperation.Compound)
        val compound = op as EditOperation.Compound
        assertEquals("outer", compound.label)
        assertEquals(3, compound.operations.size)
    }

    @Test
    fun `clear removes everything`() {
        val stack = UndoRedoStackImpl()
        stack.push(EditOperation.Insert(0, "a"))
        stack.push(EditOperation.Insert(1, "b"))
        stack.undo()
        stack.clear()
        assertFalse(stack.canUndo)
        assertFalse(stack.canRedo)
    }

    @Test
    fun `empty transaction does not create compound`() {
        val stack = UndoRedoStackImpl()
        stack.beginTransaction("empty")
        stack.endTransaction()
        assertFalse(stack.canUndo)
    }

    @Test
    fun `compound operation startOffset`() {
        val compound = EditOperation.Compound(
            listOf(
                EditOperation.Insert(0, "a"),
                EditOperation.Insert(1, "b"),
            ),
            null,
        )
        assertEquals(0, compound.startOffset)
    }

    @Test
    fun `integration - undo insert on buffer`() {
        val buf = PieceTableBuffer("")
        val undoStack = UndoRedoStackImpl()

        buf.addListener(object : TextChangeListener {
            override fun onTextChanged(event: TextChangeEvent) {
                val op = EditOperation.Insert(event.startOffset, buf.getSlice(event.startOffset, event.startOffset + event.newLength))
                undoStack.push(op)
            }
        })

        buf.insert(0, "hello")
        assertEquals("hello", buf.getText().toString())

        val undoOp = undoStack.undo() as EditOperation.Insert
        buf.delete(undoOp.startOffset, undoOp.startOffset + undoOp.text.length)
        assertEquals("", buf.getText().toString())
    }

    @Test
    fun `integration - undo delete on buffer`() {
        val buf = PieceTableBuffer("hello world")
        val undoStack = UndoRedoStackImpl()

        val deleted = buf.getSlice(5, 11)
        buf.delete(5, 11)
        undoStack.push(EditOperation.Delete(5, deleted))

        assertEquals("hello", buf.getText().toString())

        val undoOp = undoStack.undo() as EditOperation.Delete
        buf.insert(undoOp.startOffset, undoOp.deletedText)
        assertEquals("hello world", buf.getText().toString())
    }
}
