package com.aesean.activitystack

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_holder_main.view.*
import java.util.*

@Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class HideInMainActivity

@HideInMainActivity
class MainActivity : BaseActivity() {

    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()

        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        val data: MutableList<MainData> = ArrayList()
        packageInfo.activities.forEach { info ->
            if (info != null) {
                try {
                    if (shouldShow(info.name)) {
                        data.add(MainDataImpl(info))
                    }
                } catch (e: Exception) {
                    e.message.toToast()
                }
            }
        }
        data.sortBy { it.getTitle() }

        adapter = MainAdapter(data)
        recyclerView.adapter = adapter
    }

    private fun shouldShow(name: String?): Boolean {
        if (name.isNullOrBlank()) {
            return false
        }
        val clazz = Class.forName(name)
        val annotation = clazz.getAnnotation(HideInMainActivity::class.java)
        return annotation == null
    }

    private inner class MainAdapter(data: List<MainData>) : RecyclerView.Adapter<MainViewHolder>() {
        private val mData: List<MainData> = data

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
            val holder = MainViewHolder(parent)
            log("onCreateViewHolder: ${holder.hashCode()}")
            return holder
        }

        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
            log("onBindViewHolder: ${holder.hashCode()}")
            holder.bindData(mData[position])
        }

        override fun getItemCount(): Int {
            return mData.size
        }
    }

    private inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var data: MainData? = null

        constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_main, parent, false))

        init {
            itemView.setOnClickListener { view ->
                if (data != null) {
                    val targetName = data!!.getTargetName() ?: ""
                    val packageName = data!!.getPackageName() ?: ""
                    val intent = Intent()
                    intent.`package` = packageName
                    intent.setClassName(packageName, targetName)
                    try {
                        view.context.startActivity(intent)
                    } catch (e: Exception) {
                        e.message?.toToast()
                    }
                }
            }
        }

        fun bindData(data: MainData) {
            this.data = data
            itemView.titleView.text = data.getTitle()
            itemView.contentView.text = data.getContent()
        }
    }

    private interface MainData {
        fun getTitle(): String?
        fun getContent(): String?
        fun getTargetName(): String?
        fun getPackageName(): String?
    }

    private class MainDataImpl(val info: ActivityInfo) : MainData {
        override fun getPackageName(): String? {
            return info.packageName
        }

        override fun getTargetName(): String? {
            return info.name
        }

        override fun getTitle(): String? {
            val lastIndex = info.name.lastIndexOf(".")
            if (lastIndex < 0) {
                return info.name
            }
            return info.name.substring(lastIndex + 1)
        }

        override fun getContent(): String? {
            return info.name.replace(info.packageName, "")
        }
    }
}
