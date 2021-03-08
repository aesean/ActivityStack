package com.aesean.activitystack.demo.activitystack

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity

class ActivityStackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stack)
        findViewById<Button>(R.id.actionButton).setOnClickListener {
            val activityStackDetails = ActivityStackUtils.getActivityStackDetails()
            findViewById<TextView>(R.id.activityStackView).text = activityStackDetails
        }
    }
}
