package com.aesean.activitystack.demo.recyclerview

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity
import com.aesean.activitystack.view.recyclerview.ListAdapter

class ListAdapterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_adapter)
        val list = mutableListOf<Any>()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val listAdapter = ListAdapter()
        listAdapter.enableViewTypeCheck = true
        recyclerView.adapter = listAdapter
        listAdapter.registerViewRes<Int> { R.layout.view_holder_simple_1 }
            .onViewCreated { view, viewHolder, dataHolder ->
                view.setOnClickListener {
                    val data = dataHolder()
                    toast("Click: dataType Int, dataValue = $data, position = ${viewHolder.adapterPosition}")
                }
                view.setOnLongClickListener {
                    list.removeAt(viewHolder.bindingAdapterPosition)
                    listAdapter.submitList(list)
                    false
                }
            }
            .onBindView { view, data ->
                (view as TextView).text = "Int($data)"
            }

        listAdapter.register(CharSequence::class.java)
            .layoutRes { R.layout.view_holder_simple_1 }
            .onViewCreated { view, viewHolder, dataHolder ->
                view.setOnClickListener {
                    val data = dataHolder()
                    toast("Click: dataType String, position = ${viewHolder.adapterPosition}, dataValue = $data")
                }
                view.setOnLongClickListener {
                    list.removeAt(viewHolder.bindingAdapterPosition)
                    listAdapter.submitList(list)
                    false
                }
            }
            .onBindView { view, data ->
                (view as TextView).text = "String($data)"
            }

        listAdapter.register(Pair::class.java)
            .layoutRes { R.layout.view_holder_simple_2 }
            .onViewCreated { view, viewHolder, dataHolder ->
                view.setOnClickListener {
                    val data = dataHolder()
                    toast("Click: dataType Pair, dataValue = $data, position = ${viewHolder.adapterPosition}")
                }
                view.setOnLongClickListener {
                    list.removeAt(viewHolder.bindingAdapterPosition)
                    listAdapter.submitList(list)
                    false
                }
            }
            .onBindView { view, data ->
                view.findViewById<TextView>(R.id.title).text = "Pair(${data.first})"
                view.findViewById<TextView>(R.id.desc).text = "${data.second}"
            }

        findViewById<View>(R.id.add0).setOnClickListener {
            list.add(list.size)
            listAdapter.submitList(list)
        }

        findViewById<View>(R.id.add1).setOnClickListener {
            list.add("${list.size}")
            listAdapter.submitList(list)
        }

        findViewById<View>(R.id.add2).setOnClickListener {
            list.add(Pair("Title", "Value(${list.size})"))
            listAdapter.submitList(list)
        }
    }

}
