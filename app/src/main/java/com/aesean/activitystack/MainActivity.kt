package com.aesean.activitystack

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aesean.activitystack.base.BaseActivity
import com.aesean.activitystack.view.recyclerview.ListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_holder_main.view.*

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
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()
        val listAdapter = ListAdapter()
        recyclerView.adapter = listAdapter

        listAdapter.register(ActivityInfo::class.java)
                .setView(R.layout.view_holder_main)
                .onViewCreated { dataHolder, view ->
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
                    view.titleView.text = data.title
                    view.contentView.text = data.name.replace(data.packageName, "")
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
