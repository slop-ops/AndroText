package com.androtext.core.render

class DecorationProviderImpl : DecorationProvider {

    private val _decorations: MutableList<Decoration> = mutableListOf()

    override val decorations: List<Decoration> get() = _decorations

    override fun addDecoration(decoration: Decoration) {
        _decorations.add(decoration)
    }

    override fun removeDecoration(decoration: Decoration) {
        _decorations.remove(decoration)
    }

    override fun clear() {
        _decorations.clear()
    }
}
