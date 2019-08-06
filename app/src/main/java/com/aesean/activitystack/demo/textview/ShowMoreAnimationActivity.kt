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

package com.aesean.activitystack.demo.textview

import android.os.Bundle
import android.view.ViewGroup
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity
import kotlinx.android.synthetic.main.activity_show_more_animation.*

class ShowMoreAnimationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_more_animation)
        TextViewSuffixWrapper(textView).apply {
            this.mainContent = getString(R.string.sample_text)
            this.suffix = "...查看更多"
            this.suffix?.apply {
                suffixColor("...".length, this.length, R.color.md_blue_500)
            }
            this.transition?.duration = 5000
            sceneRoot = this.textView.parent.parent.parent as ViewGroup
            collapse(false)
            this.textView.setOnClickListener {
                if (this.isCollapsed) {
                    expand()
                } else {
                    collapse()
                }
            }
        }
    }

}
