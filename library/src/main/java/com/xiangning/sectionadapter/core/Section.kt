package com.xiangning.sectionadapter.core

import com.xiangning.sectionadapter.isNotNull
import java.util.*

open class Section : MutableIterable<Any> {

    private val binders = mutableMapOf<Class<*>, ItemBinder<*, *>>()
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

    internal fun bind(adapter: SectionAdapter, info: SectionInfo<Section>) {
        if (sectionAdapter != null || sectionInfo != null) {
            throw IllegalStateException("Section already bind!")
        }

        sectionAdapter = adapter
        sectionInfo = info

        for (binder in binders.values) {
            adapter.viewTypeToBinders[getViewType(binder)] = binder
        }

        adapter.notifyItemRangeInserted(info.start, info.count)

        onBind(adapter, info)
    }

    internal fun unbind() {
        isNotNull(sectionAdapter, sectionInfo) { adapter, info ->
            for (binder in binders.values) {
                adapter.viewTypeToBinders.remove(getViewType(binder))
            }
            adapter.notifyItemRangeRemoved(info.start, info.count)

            sectionAdapter = null
            sectionInfo = null

            onUnbind()
        }
    }

    protected open fun onBind(adapter: SectionAdapter, info: SectionInfo<Section>) {

    }

    protected open fun onUnbind() {

    }

    internal fun getBinder(clazz: Class<*>) = binders[clazz]

    internal fun getViewType(item: Any) = getViewType(binders[item.javaClass]!!)

    private fun getViewType(binder: ItemBinder<*, *>) = Objects.hash(this, binder)


    fun getItemSize() = items.size

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(pos: Int): T {
        return items[pos] as T
    }

    operator fun set(pos: Int, item: Any) {
        items[pos] = item
        notifyItemChanged(pos)
    }

    operator fun contains(item: Any) = items.contains(item)

    fun indexOf(item: Any) = items.indexOf(item)

    fun setItems(items: Iterable<Any>): Section {
        // 先删除
        clear()
        // 再添加
        return addItems(items)
    }

    fun addItem(item: Any): Section {
        return addItems(getItemSize(), listOf(item))
    }

    fun addItem(index: Int, item: Any): Section {
        return addItems(index, listOf(item))
    }

    fun <T : Any> addItems(items: Iterable<T>): Section {
        return addItems(getItemSize(), items)
    }

    fun <T : Any> addItems(index: Int, items: Iterable<T>): Section {
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
        return this
    }

    fun removeItem(item: Any): Section {
        val index = this.items.indexOf(item)
        if (index >= 0) {
            removeItemAt(index)
        }
        return this
    }

    fun removeItemAt(index: Int): Section {
        this.items.removeAt(index)
        update { adapter, info -> adapter.notifyItemRemoved(info.toFullPosition(index)) }
        return this
    }

    fun clear() {
        val oldSize = getItemSize()
        this.items.clear()
        update { adapter, info ->
            adapter.notifyItemRangeRemoved(info.start, oldSize)
        }
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

    inline fun <reified T> foreachOnType(block: (Int, T) -> Boolean) {
        var item: Any
        for (i in 0 until getItemSize()) {
            item = this[i]
            if (item is T && block(i, item)) {
                notifyItemChanged(i)
            }
        }
    }

    override fun iterator(): MutableIterator<Any> = Iter()

    private inner class Iter : MutableIterator<Any> {

        private var index = -1

        override fun hasNext(): Boolean {
            return index + 1 < getItemSize()
        }

        override fun next(): Any {
            return get(++index)
        }

        override fun remove() {
            removeItemAt(index--)
        }

    }

}