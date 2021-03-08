package com.aesean.activitystack.demo.recyclerview

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity
import com.aesean.activitystack.view.recyclerview.MultiTypeListAdapter

class MultiTypeListAdapterActivity : BaseActivity() {

    private data class Title(val name: String)
    private data class Item(val name: String, val desc: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_type_list_adapter)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val multiTypeListAdapter = MultiTypeListAdapter()

        recyclerView.adapter = multiTypeListAdapter

        multiTypeListAdapter.register(Title::class.java)
            .layoutRes { R.layout.view_holder_simple_1 }
            .onViewCreated { view, viewHolder, dataHolder ->
                view.setOnClickListener {
                    val data = dataHolder()
                    val adapterPosition = viewHolder.adapterPosition
                    val layoutPosition = viewHolder.layoutPosition
                    val type = multiTypeListAdapter.toType(adapterPosition)
                    toast(
                        "Click: dataType String, dataValue = $data, type = $type, adapterPosition = $adapterPosition, " +
                                "layoutPosition = $layoutPosition"
                    )
                }
            }
            .onBindView { view, data ->
                (view as TextView).text = data.name
            }

        multiTypeListAdapter.register(Item::class.java)
            .layoutRes { R.layout.view_holder_simple_2 }
            .onViewCreated { view, viewHolder, dataHolder ->
                view.setOnClickListener {
                    val data = dataHolder()
                    val adapterPosition = viewHolder.adapterPosition
                    val layoutPosition = viewHolder.layoutPosition
                    val type = multiTypeListAdapter.toType(adapterPosition)
                    toast(
                        "Click: dataType Pair, dataValue = $data, type = $type, adapterPosition = $adapterPosition, " +
                                "layoutPosition = $layoutPosition"
                    )
                }
            }
            .onBindView { view, data ->
                view.findViewById<TextView>(R.id.title).text = data.name
                view.findViewById<TextView>(R.id.desc).text = data.desc
            }

        fun setItem(type: Int, size: Int) {
            multiTypeListAdapter.submitList(type, List<Any>(size) {
                if (it == 0) {
                    Title("T($type)")
                } else {
                    Item("C(${it})", "")
                }
            })
        }

        fun removeItem(type: Int, list: MutableList<Any>) {
            if (list.isNotEmpty()) {
                list.removeAt(list.size - 1)
                multiTypeListAdapter.submitList(type, list)
            }
        }

        var list0 = 0
        var list1 = 0
        var list2 = 0

        findViewById<View>(R.id.add0).setOnClickListener {
            setItem(10, ++list0)
        }

        findViewById<View>(R.id.add1).setOnClickListener {
            setItem(20, ++list1)
        }

        findViewById<View>(R.id.add2).setOnClickListener {
            setItem(30, ++list2)
        }

        findViewById<View>(R.id.remove0).setOnClickListener {
            if (list0 > 0) {
                setItem(10, --list0)
            }
        }

        findViewById<View>(R.id.remove1).setOnClickListener {
            if (list1 > 0) {
                setItem(20, --list1)
            }
        }

        findViewById<View>(R.id.remove2).setOnClickListener {
            if (list2 > 0) {
                setItem(30, --list2)
            }
        }
    }
}
