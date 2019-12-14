package com.xiangning.sectionadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 数据视图的绑定抽象类
 */
abstract class ItemBinder<T, VH : RecyclerView.ViewHolder> {

    lateinit var section: SectionAdapter.Section
        internal set

    abstract fun onCreateViewHolder(
        inflater: LayoutInflater, parent: ViewGroup
    ): VH


    abstract fun onBindViewHolder(holder: VH, item: T)


    open fun onBindViewHolder(
        holder: VH, item: T, payloads: List<Any>
    ) {
        onBindViewHolder(holder, item)
    }


    fun getPosition(holder: RecyclerView.ViewHolder): Int {
        return holder.adapterPosition
    }


    open fun getItemId(item: T): Long {
        return RecyclerView.NO_ID
    }


    open fun onViewRecycled(holder: VH) {}


    open fun onFailedToRecycleView(holder: VH): Boolean {
        return false
    }


    open fun onViewAttachedToWindow(holder: VH) {}


    open fun onViewDetachedFromWindow(holder: VH) {}
}

