/*
 *    Copyright (C) 2017.  Aesean
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.aesean.activitystack

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.aesean.activitystack.extensions.setMaxLinesWithAnimation

class ShowMoreAnimationActivityActivity : AppCompatActivity() {

    protected fun <T : View?> Int.toView(): T {
        return findViewById<T>(this)
    }

    private lateinit var mTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_more_animation_activity)
        mTextView = R.id.target_text_view.toView()
        mTextView.setOnClickListener {
            toggle()
        }
    }

    fun toggle(view: View) {
        toggle()
    }

    private var mAnimator: ValueAnimator? = null

    private fun toggle() {
        mAnimator?.cancel()
        mAnimator = mTextView.setMaxLinesWithAnimation(if (mTextView.maxLines == 3) {
            Int.MAX_VALUE
        } else {
            3
        })
    }
}
