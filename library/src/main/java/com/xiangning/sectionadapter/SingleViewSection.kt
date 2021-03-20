package com.xiangning.sectionadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xiangning.sectionadapter.core.Section

/**
 * 直接展示View的Section，可用于Header等类型
 */
class SingleViewSection(val view: View) : Section() {

    private val binder = SingleItemBinder<String>(view)

    init {
        addLinker(String::class.java, binder)
        addItem("")
    }

    override fun onUnregister() {
        super.onUnregister()
        binder.holder?.setIsRecyclable(true)
    }

    /**
     * 复用单个ViewHolder的ItemBinder，对应的数据只能有一个，可用于Header等类型
     */
    class SingleItemBinder<T : Any>(view: View) : SimpleItemBinder<T>({ _, _ -> view }) {

        internal var holder: GeneralViewHolder? = null

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