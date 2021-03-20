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


        // 头部分组，SingleViewSection实现是一个固定的view，是Section的子类
        val header = SingleViewSection(View.inflate(this, R.layout.layout_header, null))
        // 注册分组到adapter的头部，因为现在adapter中还没有其他分组，其实第一个参数可以省略的
        adapter.register(0, header)


        // 内容分组，一个分组可以添加多个类型的数据视图绑定，下面创建两种
        // 这里一个分组就类似于MultiType的概念，可以做混排

        // 第一种，使用SimpleItemBinder创建String型的数据到视图的绑定
        // 更复杂的绑定关系可以继承ItemBinder实现，提供更多功能
        val stringBinder = SimpleItemBinder<String>(viewProvider = { inflater, parent ->
            TextView(this)
        }) { holder, item ->
            (holder.itemView as TextView).text = "内容1 $item"
        }

        // 第二种，使用SimpleItemBinder创建Integer型的数据到视图的绑定
        val intBinder = SimpleItemBinder<Integer>({ inflater, parent ->
            inflater.inflate(R.layout.layout_content, parent, false)
        }) { holder, item ->
            // 相当于onBind，holder.get方法由GeneralViewHolder提供，使用了hashmap进行缓存
            holder.get<ImageView>(R.id.icon).setBackgroundColor(item.toInt())
            holder.get<TextView>(R.id.text).text = "内容2 图文 颜色=$item"
        }

        // 创建内容分组，添加类型映射
        val content = Section()
            .addLinker(String::class.java, stringBinder)
            .addLinker(Integer::class.java, intBinder)

        // 可以先于register对分组设置数据，只能设置注册过的数据类型
        // 这里先设置一些Integer类型的数据
        content.setItems((1..5).map { Random.Default.nextInt() })

        // 注册分组到adapter
        adapter.register(content)

        // 也可以注册后再对分组数据进行增删，只需要持有分组的引用调用相应方法即可进行数据更改
        // 也不需要去使用adapter的notify，内部自动处理
        content.addItems((1..5).map { "字符串$it" })


        val footer = SingleViewSection(TextView(this).apply { text = "尾部" })
        adapter.register(footer)

        var attached = true
        footer.view.setOnClickListener {
            if (attached.also { attached = !it }) {
                adapter.unregister(header)
            } else {
                adapter.register(0, header)
            }
        }

    }
}
