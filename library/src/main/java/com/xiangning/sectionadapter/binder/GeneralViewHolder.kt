package com.xiangning.sectionadapter.binder

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

/**
 * 通用的ViewHolder，可以get获取对应id的view，GeneralViewHolder会自动缓存该id对应的View。
 * 更方便地是，如果开启kotlin拓展实验功能，则可以直接使用“GeneralViewHolder#viewId”作为变量名。
 * 只需在模块build.gradle中添加如下配置：
 *
 * androidExtensions {
 *     experimental = true
 * }
 */
class GeneralViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView), LayoutContainer {

    private val viewCache = mutableMapOf<Int, View>()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("ResourceType")
    fun <T : View> get(@IdRes id: Int): T {
        return viewCache.getOrPut(id, { itemView.findViewById<T>(id)!! }) as T
    }
}