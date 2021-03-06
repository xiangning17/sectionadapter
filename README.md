# SectionAdapter
A adapter for android recyclerview which could make a complex type view page easily.

[![](https://jitpack.io/v/xiangning17/sectionadapter.svg)](https://jitpack.io/#xiangning17/sectionadapter)


针对Android的RecyclerView的分组(Section)适配器，用于帮助我们方便地创建复杂的页面。

这个项目始于另一个类似的优秀开源项目[MultiType](https://github.com/drakeet/MultiType)，使用其来开发RecycleView确实带来了很多方便。但也是在使用过程中也还是发现有些不便，比如典型的页面大概构型是“头部+内容+尾部”的排版，一般“头部”和“尾部”不需要经常更新，内容区域要进行数据更新时就需要在一个总的数据列表（items）中去进行繁杂的截取拼接等操作，有时数据的更改和通知更新处理得不好引起的不同步还会导致错误，而且很难做到局部更新，通常都是使用notifyDataSetChanged进行全量更新。

究其原因是其本身就是设计为全混排的模式，没有一个“**分组（Section）**”的概念。分组可以按照设定的顺序进行排列，针对上面说到的情况，我们希望只需要对“内容”这个分组进行数据操作更新就好了。在Google官方准备推出的RecycleView 2.0版本中的[ContactAdapter](https://zhuanlan.zhihu.com/p/275635988)也是与此相似的概念，可以把多个经典的Adapter进行顺序的连接，每一个adapter就相当于一个分组。

那我又想继续使用MultiType提供的这种方便地类型视图绑定及混排，又想要能够有一定的分组概念供我清晰的管理页面分区，于是就有了**SectionAdapter**。

## 开始（Getting started）

1. 在项目根目录的build.gradle中添加jitpack的maven仓库:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. 在模块依赖中声明库依赖：
```groovy
dependencies {
    implementation 'com.github.xiangning17:sectionadapter:1.0.5'
}
```

## 使用方式（Usage）

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
      	
      	val adapter = SectionAdapter()

        val recycler = findViewById<RecyclerView>(R.id.recycler)
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
        // 当注册到adapter后，这些数据才渲染成视图
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
            if (attached.also { attached = !it }) {
                // unregister后，分组还可用于下次register 
                adapter.unregister(header)
            } else {
                adapter.register(0, header)
            }
            
            // 测试删除复用的Section
            // adapter.unregister(borrow)
           
            // 在某个类型上进行迭代
            // content.foreachOnType<String> { i, item ->
            //    // 如果item是引用类型，可以修改其内容
            //    // 但这里是String不可变类型，所以用了[]（set操作符）进行内容更新
            //    content[i] = "修改内容$1"
            //    // 返回true代表有改动，需要notify刷新
            //    true
            }
        }

    }
}
```

主要的使用方式就是这样了，实现的效果如下：

<img width="360" alt="snapshot" src="https://user-images.githubusercontent.com/27916852/111868841-3409a580-89b7-11eb-8af9-659fe0469247.png">


## 其他（more）

总的来说，相较于MultiType的完全混排，提炼了一个 **分组(Section)** 的概念。分组可以按照设定的顺序进行排列保证我们大的板块排列，同时在分组内也保留了多类型的视图映射，可以做混排，另外也对分组的数据操作做了封装，可以方便地进行修改。
