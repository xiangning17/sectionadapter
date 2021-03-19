package com.xiangning.sectionadapter.core

import com.xiangning.sectionadapter.isNotNull

open class Section {

    internal val binders = mutableMapOf<Class<*>, ItemBinder<*, *>>()
    private var sectionAdapter: SectionAdapter? = null
    var sectionInfo: SectionInfo<Section>? = null

    private var items: MutableList<Any> = mutableListOf()

    fun <T : Any> addLinker(clazz: Class<T>, binder: ItemBinder<T, *>): Section {
        return addLinker(Linker(clazz, binder))
    }

    fun <T : Any> addLinker(linker: Linker<T>): Section {
        binders[linker.clazz] = linker.binder
        return this
    }

    internal fun onRegister(adapter: SectionAdapter, info: SectionInfo<Section>) {
        sectionAdapter = adapter
        sectionInfo = info
        adapter.notifyItemRangeInserted(info.start, info.count)
    }

    internal fun onUnregister() {
        isNotNull(sectionAdapter, sectionInfo) { adapter, info ->
            adapter.notifyItemRangeRemoved(info.start, info.count)
        }
        sectionAdapter = null
        sectionInfo = null
    }

    internal fun getBinder(clazz: Class<*>) = binders[clazz]


    fun getItemSize() = items.size

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(pos: Int): T {
        return items[pos] as T
    }

    operator fun set(pos: Int, item: Any) {
        items[pos] = item
        notifyItemChanged(pos)
    }

    fun setItems(items: Iterable<Any>) {
        // 先删除
        val oldSize = getItemSize()
        this.items.clear()
        update { adapter, info ->
            adapter.notifyItemRangeRemoved(info.start, oldSize)
        }
        // 再添加
        addItems(items)
    }

    fun addItem(item: Any) {
        addItems(getItemSize(), listOf(item))
    }

    fun addItem(index: Int, item: Any) {
        addItems(index, listOf(item))
    }

    fun <T : Any> addItems(items: Iterable<T>) {
        addItems(getItemSize(), items)
    }

    fun <T : Any> addItems(index: Int, items: Iterable<T>) {
        var i = index
        for (item in items) {
            if (item.javaClass in binders) {
                this.items.add(i++, item)
            } else {
                throw RuntimeException("item type has not linked! ${item.javaClass}, linked class are ${binders.keys}")
            }
        }

        update { adapter, info ->
            adapter.notifyItemRangeInserted(info.toFullPosition(index), i - index)
        }
    }

    fun removeItem(item: Any) {
        val index = this.items.indexOf(item)
        if (index >= 0) {
            removeAt(index)
        }
    }

    fun removeAt(index: Int) {
        this.items.removeAt(index)
        update { adapter, info -> adapter.notifyItemRemoved(info.toFullPosition(index)) }
    }

    private inline fun update(notify: (adapter: SectionAdapter, info: SectionInfo<Section>) -> Unit) {
        isNotNull(sectionAdapter, sectionInfo) { adapter, info ->
            adapter.updateSectionSize(this)
            notify(adapter, info)
        }
    }

    fun notifyItemChanged(position: Int) {
        isNotNull(sectionAdapter, sectionInfo) { adapter, info ->
            adapter.notifyItemChanged(info.toFullPosition(position))
        }
    }

    fun notifyItemRangeChanged(position: Int, count: Int) {
        isNotNull(sectionAdapter, sectionInfo) { adapter, info ->
            adapter.notifyItemRangeChanged(info.toFullPosition(position), count)
        }
    }

    fun notifyItemMoved(fromPosition: Int, toPosition: Int) {
        isNotNull(sectionAdapter, sectionInfo) { adapter, info ->
            adapter.notifyItemMoved(
                info.toFullPosition(fromPosition),
                info.toFullPosition(toPosition)
            )
        }
    }

}