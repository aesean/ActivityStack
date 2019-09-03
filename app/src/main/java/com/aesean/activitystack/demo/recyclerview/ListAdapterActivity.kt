package com.aesean.activitystack.demo.recyclerview

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity
import com.aesean.activitystack.view.recyclerview.ListAdapter
import kotlinx.android.synthetic.main.activity_list_adapter.*
import kotlinx.android.synthetic.main.view_holder_simple_2.view.*

class ListAdapterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_adapter)

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val listAdapter = ListAdapter()
        recyclerView.adapter = listAdapter
        listAdapter.register(Int::class.java)
                .setView(R.layout.view_holder_simple_1)
                .onViewCreated { dataHolder, view ->
                    view.setOnClickListener {
                        val data = dataHolder()
                        toast("Click: dataType Int, dataValue = $data")
                    }
                }
                .onBindView { view, data ->
                    (view as TextView).text = "Int($data)"
                }

        listAdapter.register(String::class.java)
                .setView(R.layout.view_holder_simple_1)
                .onViewCreated { dataHolder, view ->
                    view.setOnClickListener {
                        val data = dataHolder()
                        toast("Click: dataType String, dataValue = $data")
                    }
                }
                .onBindView { view, data ->
                    (view as TextView).text = "String($data)"
                }

        listAdapter.register(Pair::class.java)
                .setView(R.layout.view_holder_simple_2)
                .onViewCreated { dataHolder, view ->
                    view.setOnClickListener {
                        val data = dataHolder()
                        toast("Click: dataType Pair, dataValue = $data")
                    }
                }
                .onBindView { view, data ->
                    view.title.text = "Pair(${data.first})"
                    view.desc.text = "${data.second}"
                }

        val list = mutableListOf<Any>()

        add0.setOnClickListener {
            list.add(list.size)
            listAdapter.submitList(list)
        }

        add1.setOnClickListener {
            list.add("String(${list.size})")
            listAdapter.submitList(list)
        }

        add2.setOnClickListener {
            list.add(Pair("Title", "Value(${list.size})"))
            listAdapter.submitList(list)
        }
    }

}
