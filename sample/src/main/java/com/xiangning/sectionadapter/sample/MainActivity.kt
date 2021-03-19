package com.xiangning.sectionadapter.sample

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xiangning.sectionadapter.SimpleItemBinder
import com.xiangning.sectionadapter.SingleViewSection
import com.xiangning.sectionadapter.core.Section
import com.xiangning.sectionadapter.core.SectionAdapter
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = SectionAdapter()

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler.adapter = adapter

        // 添加内容分组1，并设置数据
        val contentSection = adapter.register(
            String::class.java,
            SimpleItemBinder({ _, _ -> TextView(this) }) { holder, item ->
                (holder.itemView as TextView).text = "内容1 $item"
            })
        contentSection.setItems((1..20).map { "$it" })

        // 添加内容分组2，并设置数据
        val contentSection2 = Section()
            .addLinker(
                Integer::class.java,
                SimpleItemBinder({ inflater, parent ->
                    inflater.inflate(R.layout.layout_content, parent, false)
                }) { holder, item ->
                    holder.get<ImageView>(R.id.icon).setBackgroundColor(item.toInt())
                    holder.get<TextView>(R.id.text).text = "内容2 图文 颜色=$item"
                })
            .addLinker(
                String::class.java,
                SimpleItemBinder({ _, _ -> TextView(this) }) { holder, item ->
                    (holder.itemView as TextView).text = "内容2 文本 $item"
                })
        contentSection2.addItems((1..10).map { Random.Default.nextInt() })
        contentSection2.addItems((1..5).map { "你好$it" })
        adapter.register(contentSection2)

        val header = View.inflate(this, R.layout.layout_header, null)
        header.setOnClickListener {
            adapter.unregister(contentSection)
        }

        header.setOnLongClickListener {
            contentSection2.removeAt(0)
            true
        }

        // 插入Header分组到最前面，其实现是一个固定的view
        adapter.register(0, SingleViewSection(header))

    }
}
