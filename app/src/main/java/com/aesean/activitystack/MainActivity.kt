package com.aesean.activitystack

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.base.BaseActivity
import com.aesean.activitystack.view.recyclerview.ListAdapter

@Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class HideInMainActivity

@HideInMainActivity
class MainActivity : BaseActivity() {
    private val ActivityInfo.title: String
        get() {
            val lastIndex = this.name.lastIndexOf(".")
            if (lastIndex < 0) {
                return this.name
            }
            return this.name.substring(lastIndex + 1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()
        val listAdapter = ListAdapter()
        recyclerView.adapter = listAdapter

        listAdapter.register(ActivityInfo::class.java)
            .layoutRes { R.layout.view_holder_main }
            .onViewCreated { view, viewHolder, dataHolder ->
                view.setOnClickListener {
                    val data = dataHolder()
                    val targetName = data.name
                    val packageName = data.packageName
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
            .onBindView { view, data ->
                view.findViewById<TextView>(R.id.titleView).text = data.title
                view.findViewById<TextView>(R.id.contentView).text =
                    data.name.replace(data.packageName, "")
            }

        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        val list = packageInfo.activities.filter { shouldShow(it.name) }.sortedBy { it.title }
        listAdapter.submitList(list)
    }

    private fun shouldShow(name: String?): Boolean {
        if (name.isNullOrBlank()) {
            return false
        }
        val clazz = Class.forName(name)
        val annotation = clazz.getAnnotation(HideInMainActivity::class.java)
        return annotation == null
    }
}
