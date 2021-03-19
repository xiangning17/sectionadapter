package com.xiangning.sectionadapter.core

import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    internal var binder: ItemBinder<*, SectionViewHolder>? = null

    /**
     * 当前位于Section中的位置，该值仅在onBindViewHolder后生效
     */
    var sectionPosition: Int = -1

}