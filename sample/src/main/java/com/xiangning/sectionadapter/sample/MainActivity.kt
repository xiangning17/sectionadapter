package com.xiangning.sectionadapter.sample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xiangning.sectionadapter.SectionAdapter
import com.xiangning.sectionadapter.binder.SimpleItemBinder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = SectionAdapterExt()

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler.adapter = adapter

        val header1 = TextView(this).apply { text = "header1" }
        val header2 = TextView(this).apply { text = "header2" }
        val header3 = TextView(this).apply { text = "header3" }
        adapter.addHeader(header1)
        adapter.addHeader(header2)

        adapter.register(
            SectionAdapter.Linker(
                Integer::class.java,
                SimpleItemBinder(R.layout.layout_header)
            )
        )
            .setItems((1..30).toList())

        val footer1 = TextView(this).apply { text = "footer1" }
        val footer2 = TextView(this).apply { text = "footer2" }
        val footer3 = TextView(this).apply { text = "footer3" }
        adapter.addFooter(footer1)
        adapter.addFooter(footer2)


        header1.postDelayed({
            adapter.removeHeader(header2)
            adapter.addHeader(header3)
            adapter.addFooter(footer3)
        }, 3000)

    }
}
