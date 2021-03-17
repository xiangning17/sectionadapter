package com.xiangning.sectionadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import java.lang.IndexOutOfBoundsException
import kotlin.math.max
import kotlin.math.min

/**
 * 用于RecyclerView的分组适配器，根据注册顺序生成不同的分组，每个分组可单独管理数据。
 */
open class SectionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<Any>()

    private val sectionCounter = SectionListCounter<Section>()
    private val viewTypeToBinders = mutableMapOf<Int, ItemBinder<*, *>>()

    data class Linker<T : Any>(val clazz: Class<T>, val binder: ItemBinder<T, *>)

    inner class Section internal constructor(private val linkers: List<Linker<*>>) {

        internal val binders = mutableMapOf<Class<*>, ItemBinder<*, *>>()

        init {
            check(linkers.isNotEmpty()) { "linkers不能为空" }
            for ((clazz, binder) in linkers) {
                binders[clazz] = binder
                binder.section = this
            }
        }

        internal fun getBinder(clazz: Class<*>) = binders[clazz]

        fun getLinkerSize() = linkers.size

        fun <T : Any> setItems(vararg items: List<T>) {
            setItemsWithoutCheck(this,
                items.flatMap { group -> group.filter { it.javaClass in binders } })
        }

        fun getItems(): List<Any> {
            val (_, pos, count) = sectionCounter.getSectionInfo(this)!!
            return items.slice(pos until pos + count)
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> getItem(sectionPos: Int): T {
            val (_, pos, count) = sectionCounter.getSectionInfo(this)!!
            if (sectionPos !in 0 until count) {
                throw IndexOutOfBoundsException("超出范围：size=$count, pos=$sectionPos")
            }
            return items[pos + sectionPos] as T
        }

        fun getItemSize() = sectionCounter.getSectionInfo(this)!!.count

        fun getInfo() = sectionCounter.getSectionInfo(this)!!

        fun toRealPosition(sectionPos: Int): Int {
            val (_, pos, count) = sectionCounter.getSectionInfo(this)!!
            return pos + max(0, min(count, sectionPos))
        }

        fun notifyItemsChanged() {
            val info = sectionCounter.getSectionInfo(this)!!
            this@SectionAdapter.notifyItemRangeChanged(info.position, info.count)
        }

        @JvmOverloads
        fun notifyItemChanged(position: Int, payload: Any? = null) {
            this@SectionAdapter.notifyItemChanged(toRealPosition(position), payload)
        }

        @JvmOverloads
        fun notifyItemRangeChanged(position: Int, count: Int, payload: Any? = null) {
            this@SectionAdapter.notifyItemRangeChanged(toRealPosition(position), count, payload)
        }

        fun notifyItemInserted(position: Int) {
            this@SectionAdapter.notifyItemInserted(toRealPosition(position))
        }

        fun notifyItemRangeInserted(position: Int, count: Int) {
            this@SectionAdapter.notifyItemRangeInserted(toRealPosition(position), count)
        }

        fun notifyItemRemoved(position: Int) {
            this@SectionAdapter.notifyItemRemoved(toRealPosition(position))
        }

        fun notifyItemRangeRemoved(position: Int, count: Int) {
            this@SectionAdapter.notifyItemRangeRemoved(toRealPosition(position), count)
        }

        fun notifyItemMoved(fromPosition: Int, toPosition: Int) {
            this@SectionAdapter.notifyItemRangeRemoved(
                toRealPosition(fromPosition),
                toRealPosition(toPosition)
            )

        }
    }

    @JvmOverloads
    fun <T : Any> register(clazz: Class<T>, binder: ItemBinder<T, *>, index: Int = -1): Section {
        return register(Linker(clazz, binder), index = index)
    }

    @JvmOverloads
    fun register(vararg linkers: Linker<*>, index: Int = -1) =
        register(Section(linkers.toList()), index)

    @JvmOverloads
    fun register(section: Section, index: Int = -1): Section {
        sectionCounter.register(index, section)
        for (binder in section.binders.values) {
            viewTypeToBinders[binder.hashCode()] = binder
        }
        return section
    }

    fun unregister(section: Section) {
        section.setItems(emptyList())
        sectionCounter.unregister(section)
        // onViewDetachedFromWindow callback needs the cache later, so keep it.
        // for (binder in section.binders.values) {
        //     viewTypeToBinders.remove(binder.hashCode())
        // }
    }

    fun getSectionInfoOfPosition(position: Int): SectionInfo<Section>? {
        return sectionCounter.getSectionInfo(position)
    }

    private fun setItemsWithoutCheck(section: Section, newTypeItems: List<Any>) {
        val (_, sectionPos, sectionSize) = sectionCounter.getSectionInfo(section)!!

        // update type counter
        sectionCounter.setSize(section, newTypeItems.size)

        val oldItems = items
        val callback = object : DiffUtil.Callback() {

            override fun getOldListSize() = sectionSize

            override fun getNewListSize() = newTypeItems.size

            override fun areItemsTheSame(oldPositon: Int, newPosition: Int) =
                oldItems[sectionPos + oldPositon] === newTypeItems[newPosition]

            override fun areContentsTheSame(oldPositon: Int, newPosition: Int) =
                oldItems[sectionPos + oldPositon] == newTypeItems[newPosition]

        }

        // replace the old items with new items
        DiffUtil.calculateDiff(callback).dispatchUpdatesTo(
            AdapterListUpdateCallback(
                this,
                sectionPos
            )
        )
        items = oldItems.slice(0 until sectionPos) +
                newTypeItems +
                oldItems.slice(sectionPos + sectionSize until oldItems.size)

    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getItem(position: Int): T? {
        if (position !in items.indices) {
            return null
        }

        return items[position] as T?
    }


    /**
     * 加入了偏移修正的ListUpdateCallback
     *
     * @see DiffUtil.DiffResult.dispatchUpdatesTo
     */
    private class AdapterListUpdateCallback(
        private val mAdapter: RecyclerView.Adapter<*>,
        private val offset: Int
    ) :
        ListUpdateCallback {

        override fun onInserted(position: Int, count: Int) {
            mAdapter.notifyItemRangeInserted(offset + position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            mAdapter.notifyItemRangeRemoved(offset + position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            mAdapter.notifyItemMoved(offset + fromPosition, offset + toPosition)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            mAdapter.notifyItemRangeChanged(offset + position, count, payload)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return sectionCounter.getSection(position)!!
            .getBinder(items[position]::class.java).hashCode()
    }

    override fun getItemId(position: Int): Long {
        return RecyclerView.NO_ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return getOutBinderByViewType(viewType).onCreateViewHolder(inflater, parent)
    }

    @Deprecated(
        "You should not call this method.",
        ReplaceWith("RecyclerView.Adapter#onBindViewHolder(holder, position, payloads)")
    )
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        throw IllegalAccessError(
            "You should not call this method. " +
                    "Call RecyclerView.Adapter#onBindViewHolder(holder, position, payloads) instead."
        )
    }

    @SuppressWarnings("unchecked")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        getOutBinderByViewType(holder.itemViewType).onBindViewHolder(
            holder,
            items[position],
            payloads
        )
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        getOutBinderByViewType(holder.itemViewType).onViewDetachedFromWindow(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return getOutBinderByViewType(holder.itemViewType).onFailedToRecycleView(holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        getOutBinderByViewType(holder.itemViewType).onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        getOutBinderByViewType(holder.itemViewType).onViewAttachedToWindow(holder)
    }

    private fun getOutBinderByViewType(viewType: Int): ItemBinder<Any, RecyclerView.ViewHolder> {
        @Suppress("UNCHECKED_CAST")
        return viewTypeToBinders[viewType] as ItemBinder<Any, RecyclerView.ViewHolder>
    }
}