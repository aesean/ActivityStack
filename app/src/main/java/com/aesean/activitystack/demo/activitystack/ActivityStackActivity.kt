package com.aesean.activitystack.demo.activitystack

import android.os.Bundle
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity
import kotlinx.android.synthetic.main.activity_stack.*

class ActivityStackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stack)
        actionButton.setOnClickListener {
            val activityStackDetails = ActivityStackUtils.getActivityStackDetails()
            activityStackView.text = activityStackDetails
        }
    }
}
