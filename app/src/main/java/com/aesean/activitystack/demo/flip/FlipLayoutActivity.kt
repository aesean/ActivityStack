package com.aesean.activitystack.demo.flip

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aesean.activitystack.R
import com.aesean.activitystack.view.flip.FlipLayout

class FlipLayoutActivity : AppCompatActivity() {
    private fun log(s: String?) {
        Log.d("FlipLayoutActivity", s.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flip_layout)
        val view = findViewById<FlipLayout>(R.id.flipLayout)
        view.flipInDuration = 3000
        view.flipOutDuration = 3000
        view.setOnClickListener {
            view.flip()
        }

        view.setFlipListener(object : FlipLayout.FlipListenerAdapter() {
            override fun onFlipStart(from: Int, to: Int) {
                super.onFlipStart(from, to)
                for (index in 0 until view.childCount) {
                    val childView = view.getChildAt(index)
                    log("onFlipStart($from, $to): index = $index, pivotX = ${childView.pivotX}, pivotY = ${childView.pivotY}, translationX = ${childView.translationX}")
                }
            }

            override fun onFlipInStart(index: Int) {
                super.onFlipInStart(index)
                log("onFlipInStart: $index")
            }

            override fun onFlipInEnd(index: Int) {
                super.onFlipInEnd(index)
                log("onFlipInEnd: $index")
            }

            override fun onFlipOutStart(index: Int) {
                super.onFlipOutStart(index)
                log("onFlipOutStart: $index")
            }

            override fun onFlipOutEnd(index: Int) {
                super.onFlipOutEnd(index)
                log("onFlipOutEnd: $index")
            }

            override fun onFlipEnd(from: Int, to: Int) {
                super.onFlipEnd(from, to)
                for (index in 0 until view.childCount) {
                    val childView = view.getChildAt(index)
                    log("onFlipEnd($from, $to): index = $index, pivotX = ${childView.pivotX}, pivotY = ${childView.pivotY}, translationX = ${childView.translationX}")
                }
            }
        })

        findViewById<View>(R.id.flip).setOnClickListener {
            view.flip()
        }
        findViewById<View>(R.id.flipReverse).setOnClickListener {
            view.flipReverse()
        }
        findViewById<View>(R.id.resume).setOnClickListener {
            view.resume()
        }
        findViewById<View>(R.id.pause).setOnClickListener {
            view.pause()
        }
    }
}
