package com.xiangning.sectionadapter.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 用于RecyclerView的分组适配器，根据注册顺序生成不同的分组，每个分组可单独管理数据。
 */
open class SectionAdapter : RecyclerView.Adapter<SectionViewHolder>() {

    private val sectionCounter = SectionListCounter<Section>()
    private val viewTypeToBinders = mutableMapOf<Int, ItemBinder<*, *>>()

    fun <T : Any> register(clazz: Class<T>, binder: ItemBinder<T, *>): Section {
        return register(-1, clazz, binder)
    }

    fun <T : Any> register(index: Int, clazz: Class<T>, binder: ItemBinder<T, *>): Section {
        return register(index, Section().addLinker(clazz, binder))
    }

    fun register(section: Section): Section {
        return register(-1, section)
    }

    fun register(index: Int, section: Section): Section {
        for (binder in section.binders.values) {
            viewTypeToBinders[binder.hashCode()] = binder
        }
        sectionCounter.register(index, section, section.getItemSize())
        section.onRegister(this, sectionCounter.getSectionInfo(section)!!)
        return section
    }

    fun unregister(section: Section) {
        for (binder in section.binders.values) {
            viewTypeToBinders.remove(binder.hashCode())
        }
        sectionCounter.unregister(section)
        section.onUnregister()
    }

    internal fun updateSectionSize(section: Section) {
        sectionCounter.setSize(section, section.getItemSize())
    }

    override fun getItemCount(): Int {
        return sectionCounter.getCount()
    }

    override fun getItemViewType(position: Int): Int {
        return sectionCounter.getSectionInfoByPos(position)!!.let { info ->
            val section = info.section
            val item: Any = section[info.toSectionPos(position)]
            section.getBinder(item::class.java).hashCode()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        @Suppress("UNCHECKED_CAST")
        val binder = viewTypeToBinders[viewType] as ItemBinder<*, SectionViewHolder>
        return binder.onCreateViewHolder(inflater, parent).also { it.binder = binder }
    }

    @Deprecated(
        "You should not call this method.",
        ReplaceWith("RecyclerView.Adapter#onBindViewHolder(holder, position, payloads)")
    )
    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        throw IllegalAccessError(
            "You should not call this method. " +
                    "Call RecyclerView.Adapter#onBindViewHolder(holder, position, payloads) instead."
        )
    }

    override fun onBindViewHolder(
        holder: SectionViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        @Suppress("UNCHECKED_CAST")
        val binder = holder.binder as ItemBinder<Any, SectionViewHolder>

        sectionCounter.getSectionInfoByPos(position)!!.let { info ->
            val section = info.section
            val sectionPosition = info.toSectionPos(position)
            val item: Any = section[sectionPosition]

            holder.sectionPosition = sectionPosition
            binder.onBindViewHolder(holder, item, payloads)
        }
    }

    override fun onViewDetachedFromWindow(holder: SectionViewHolder) {
        holder.binder?.onViewDetachedFromWindow(holder)
    }

    override fun onFailedToRecycleView(holder: SectionViewHolder): Boolean {
        return holder.binder!!.onFailedToRecycleView(holder)
    }

    override fun onViewRecycled(holder: SectionViewHolder) {
        holder.binder?.onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: SectionViewHolder) {
        holder.binder?.onViewAttachedToWindow(holder)
    }

}