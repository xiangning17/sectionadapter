package com.xiangning.sectionadapter.sample

import android.view.View
import com.xiangning.sectionadapter.SectionAdapter
import com.xiangning.sectionadapter.binder.SimpleItemBinder

class SectionAdapterExt : SectionAdapter() {

    private val headers = mutableListOf<Section>()
    private val footers = mutableListOf<Section>()
    private var empty: View? = null
    private val items = mutableMapOf<View, Section>()


    fun addHeader(header: View) {
        addType(headers, header, headers.size)
    }

    fun removeHeader(header: View) {
        removeType(headers, header)
    }

    fun addFooter(footer: View) {
        addType(footers, footer, -1)
    }

    fun removeFooter(footer: View) {
        removeType(footers, footer)
    }

    private fun addType(group: MutableList<Section>, view: View, index: Int) {
        val section = register(Integer::class.java, SimpleItemBinder({ _, _ -> view }), index)
        section.setItems(listOf(group.size))
        group.add(section)
        items[view] = section
    }

    private fun removeType(group: MutableList<Section>, view: View) {
        items.remove(view)?.let {
            group.remove(it)
            unregister(it)
        }
    }

}