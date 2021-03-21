package com.xiangning.sectionadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.xiangning.sectionadapter.core.ItemBinder

/**
 * @author xiangning
 * @Date 2019-11-22
 * @description 方便创建ItemBinder的工具类
 */
open class SimpleItemBinder<T : Any>() : ItemBinder<T, GeneralViewHolder>() {

    private var layout: Int = 0

    private var viewProvider: ((inflater: LayoutInflater, parent: ViewGroup) -> View)? = null

    private var bindHolder: ((holder: GeneralViewHolder, item: T) -> Unit)? = null

    @JvmOverloads
    constructor(
        @LayoutRes layout: Int,
        bindHolder: ((holder: GeneralViewHolder, item: T) -> Unit)? = null
    ) : this(layout, null, bindHolder)

    @JvmOverloads
    constructor(
        viewProvider: ((inflater: LayoutInflater, parent: ViewGroup) -> View),
        bindHolder: ((holder: GeneralViewHolder, item: T) -> Unit)? = null
    ) : this(0, viewProvider, bindHolder)

    private constructor(
        @LayoutRes layout: Int = 0,
        viewProvider: ((inflater: LayoutInflater, parent: ViewGroup) -> View)? = null,
        bindHolder: ((holder: GeneralViewHolder, item: T) -> Unit)? = null
    ) : this() {
        check(layout != 0 || viewProvider != null)
        this.layout = layout
        this.viewProvider = viewProvider
        this.bindHolder = bindHolder
    }

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): GeneralViewHolder {
        return GeneralViewHolder(
            viewProvider?.invoke(inflater, parent) ?: inflater.inflate(
                layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GeneralViewHolder, item: T) {
        bindHolder?.invoke(holder, item)
    }

}