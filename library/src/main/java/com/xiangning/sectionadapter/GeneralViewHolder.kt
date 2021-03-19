package com.xiangning.sectionadapter

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.IdRes
import com.xiangning.sectionadapter.core.SectionViewHolder

/**
 * 通用的ViewHolder，可以get获取对应id的view，GeneralViewHolder会自动缓存该id对应的View。
 */
open class GeneralViewHolder(itemView: View) : SectionViewHolder(itemView) {

    private val viewCache = mutableMapOf<Int, View>()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("ResourceType")
    operator fun <T : View> get(@IdRes id: Int): T {
        return viewCache.getOrPut(id, { itemView.findViewById<T>(id)!! }) as T
    }
}