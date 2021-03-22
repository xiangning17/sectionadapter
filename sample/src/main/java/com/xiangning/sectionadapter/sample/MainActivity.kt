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
        // 分组在未注册到adapter是，完全可以当成一个List使用
        // 其提供了add、remove、get、迭代遍历等操作
        // 当注册到adapter后，这些数据会及时渲染
        // 而解注册后，分组数据也可保持以待后续再次注册使用
        content.setItems((1..5).map { Random.Default.nextInt() })

        // 注册分组到adapter
        adapter.register(content)

        // 也可以注册后再对分组数据进行增删，只需要持有分组的引用调用相应方法即可进行数据更改
        // 也不需要去使用adapter的notify，内部自动处理
        content.addItems((1..3).map { "字符串$it" })

        // 不同Section里复用binder，解除注册时不会影响另外的Section
        // 这里使用adapter.register快捷注册生成一个Section，并设置数据，最终返回Section自身
        val borrow = adapter.register(String::class.java, stringBinder)
            .setItems((1..3).map { "这是共用binder的内容$it" })

        // 添加一个尾部分组
        val footer = SingleViewSection(TextView(this).apply { text = "尾部" })
        adapter.register(footer)

        // 根据标志测试动态register和unregister一个分组的情况
        var attached = true
        footer.view.setOnClickListener {
//            if (attached.also { attached = !it }) {
//                // unregister后，分组还可用于下次register
//                adapter.unregister(header)
//            } else {
//                adapter.register(0, header)
//            }

            // 测试删除复用的Section
            //  adapter.unregister(borrow)

            // 在某个类型上进行迭代
            content.foreachOnType<String> { i, item ->
                // 如果item是引用类型，可以修改其内容
                // 但这里是String不可变类型，所以用了[]（set操作符）进行内容更新
                content[i] = "修改内容$1"
                // 返回true代表有改动，需要notify刷新
                true
            }
        }

    }
}
