package com.aesean.activitystack.demo.recyclerview

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity
import com.aesean.activitystack.view.recyclerview.MultiTypeListAdapter
import kotlinx.android.synthetic.main.activity_list_adapter.add0
import kotlinx.android.synthetic.main.activity_list_adapter.add1
import kotlinx.android.synthetic.main.activity_list_adapter.add2
import kotlinx.android.synthetic.main.activity_list_adapter.recyclerView
import kotlinx.android.synthetic.main.activity_multi_type_list_adapter.*
import kotlinx.android.synthetic.main.view_holder_simple_2.view.*

class MultiTypeListAdapterActivity : BaseActivity() {

    private data class Title(val name: String)
    private data class Item(val name: String, val desc: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_type_list_adapter)

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val multiTypeListAdapter = MultiTypeListAdapter()

        recyclerView.adapter = multiTypeListAdapter

        multiTypeListAdapter.register(Title::class.java)
                .setView(R.layout.view_holder_simple_1)
                .onViewCreated { dataHolder, view ->
                    view.setOnClickListener {
                        val data = dataHolder()
                        toast("Click: dataType String, dataValue = $data")
                    }
                }
                .onBindView { view, data ->
                    (view as TextView).text = data.name
                }

        multiTypeListAdapter.register(Item::class.java)
                .setView(R.layout.view_holder_simple_2)
                .onViewCreated { dataHolder, view ->
                    view.setOnClickListener {
                        val data = dataHolder()
                        toast("Click: dataType Pair, dataValue = $data")
                    }
                }
                .onBindView { view, data ->
                    view.title.text = data.name
                    view.desc.text = data.desc
                }

        fun newItem(type: Int, list: MutableList<Any>) {
            if (list.isEmpty()) {
                list.add(Title("Title($type)"))
            } else {
                list.add(Item("Content(${list.size})", "this is a simple item"))
            }
            multiTypeListAdapter.submitList(type, list)
        }

        fun removeItem(type: Int, list: MutableList<Any>) {
            if (list.isNotEmpty()) {
                list.removeAt(list.size - 1)
                multiTypeListAdapter.submitList(type, list)
            }
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

        remove0.setOnClickListener {
            removeItem(0, list0)
        }

        remove1.setOnClickListener {
            removeItem(1, list1)
        }

        remove2.setOnClickListener {
            removeItem(2, list2)
        }
    }
}
