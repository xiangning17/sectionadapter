package com.xiangning.sectionadapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xiangning.sectionadapter.binder.SimpleItemBinder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = SectionAdapter()
        val section = adapter.register(
            SectionAdapter.Linker(
                String::class.java,
                SimpleItemBinder<String>(R.layout.activity_main)
            )
        )
    }
}
