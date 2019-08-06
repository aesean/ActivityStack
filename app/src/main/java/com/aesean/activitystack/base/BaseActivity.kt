package com.aesean.activitystack.base

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    val tag = this.javaClass.simpleName

    protected fun log(any: Any?) {
        Log.d(tag, any.toString())
    }

    protected fun toastShort(s: String) {
        log("toast: $s")
        Toast.makeText(this@BaseActivity, s, Toast.LENGTH_SHORT).show()
    }

    protected fun toast(s: String) {
        log("toast: $s")
        Toast.makeText(this@BaseActivity, s, Toast.LENGTH_LONG).show()
    }

    protected fun String?.toToast() {
        log("toast: $this")
        Toast.makeText(this@BaseActivity, this@toToast.toString(), Toast.LENGTH_LONG).show()
    }
}