package com.aesean.activitystack.demo.recyclerview

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity
import com.aesean.activitystack.view.recyclerview.MultiTypeListAdapter
import kotlinx.android.synthetic.main.activity_list_adapter.*
import kotlinx.android.synthetic.main.view_holder_simple_2.view.*

class MultiTypeListAdapterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_type_list_adapter)

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val multiTypeListAdapter = MultiTypeListAdapter()

        recyclerView.adapter = multiTypeListAdapter

        multiTypeListAdapter.register(String::class.java)
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

        multiTypeListAdapter.register(Pair::class.java)
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

        fun newItem(type: Int, list: MutableList<Any>) {
            val data: Any = when ((Math.random() * 2).toInt()) {
                0 -> {
                    "TYPE($type), String(${list.size})"
                }
                1 -> {
                    Pair("TYPE($type)-Title", "Value(${list.size})")
                }
                else -> return
            }
            list.add(data)
            multiTypeListAdapter.submitList(type, list)
        }

        val list0 = mutableListOf<Any>()
        val list1 = mutableListOf<Any>()
        val list2 = mutableListOf<Any>()

        add0.setOnClickListener {
            newItem(0, list0)
        }

        add1.setOnClickListener {
            newItem(1, list1)
        }

        add2.setOnClickListener {
            newItem(2, list2)
        }
    }
}
