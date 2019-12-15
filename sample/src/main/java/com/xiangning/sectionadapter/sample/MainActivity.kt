package com.xiangning.sectionadapter.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xiangning.sectionadapter.SectionAdapter
import com.xiangning.sectionadapter.binder.SimpleItemBinder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = SectionAdapter()
        val section = adapter.register(
            SectionAdapter.Linker(
                String::class.java,
                SimpleItemBinder(R.layout.activity_main)
            )
        )
    }
}
