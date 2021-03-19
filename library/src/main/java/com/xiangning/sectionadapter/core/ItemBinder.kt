package com.xiangning.sectionadapter.core

import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * 数据视图的绑定抽象类
 */
abstract class ItemBinder<T : Any, VH : SectionViewHolder> {

    abstract fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH


    abstract fun onBindViewHolder(holder: VH, item: T)


    open fun onBindViewHolder(holder: VH, item: T, payloads: List<Any>) {
        onBindViewHolder(holder, item)
    }


    open fun onViewRecycled(holder: VH) {}


    open fun onFailedToRecycleView(holder: VH): Boolean {
        return false
    }


    open fun onViewAttachedToWindow(holder: VH) {}


    open fun onViewDetachedFromWindow(holder: VH) {}
}

