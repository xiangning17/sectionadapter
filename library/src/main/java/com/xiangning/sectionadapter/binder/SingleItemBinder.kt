package com.xiangning.sectionadapter.binder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/**
 * 复用单个ViewHolder的ItemBinder，对应的数据只能有一个，可用于Header等类型
 */
class SingleItemBinder<T> : SimpleItemBinder<T> {

    private var holder: GeneralViewHolder? = null

    constructor(
        @LayoutRes layout: Int,
        bindHolder: ((holder: GeneralViewHolder, item: T) -> Unit)? = null
    ) : super(layout, bindHolder)

    constructor(
        viewProvider: ((inflater: LayoutInflater, parent: ViewGroup) -> View),
        bindHolder: ((holder: GeneralViewHolder, item: T) -> Unit)? = null
    ) : super(viewProvider, bindHolder)

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): GeneralViewHolder {
        return (holder ?: super.onCreateViewHolder(inflater, parent).apply {
            setIsRecyclable(false)
            holder = this
        })
    }

}