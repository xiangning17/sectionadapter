package com.xiangning.sectionadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xiangning.sectionadapter.core.Section

/**
 * 直接展示View的Section，可用于Header等类型
 */
class SingleViewSection(view: View) : Section() {

    init {
        addLinker(Integer::class.java, SingleItemBinder(view))
        addItem(0)
    }

    /**
     * 复用单个ViewHolder的ItemBinder，对应的数据只能有一个，可用于Header等类型
     */
    class SingleItemBinder<T : Any>(view: View) : SimpleItemBinder<T>({ _, _ -> view }) {

        private var holder: GeneralViewHolder? = null

        override fun onCreateViewHolder(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): GeneralViewHolder {
            return holder ?: super.onCreateViewHolder(inflater, parent).apply {
                setIsRecyclable(false)
                holder = this
            }
        }

    }
}