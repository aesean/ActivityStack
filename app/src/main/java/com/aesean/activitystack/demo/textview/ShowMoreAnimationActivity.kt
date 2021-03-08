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
import android.view.View
import android.view.ViewGroup
import com.aesean.activitystack.R
import com.aesean.activitystack.base.BaseActivity

class ShowMoreAnimationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_more_animation)
        val textViewSuffixWrapper =
            TextViewSuffixWrapper(findViewById(R.id.textView)).apply wrapper@{
                // this.mainContent = getString(R.string.sample_text)
                this.mainContent =
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
                // this.mainContent = "caption>>caption{filterTagId3=filterTagName3, filterTagId4=filterTagName4, filterTagId1=filterTagName1, filterTagId2=filterTagName2, filterTagId0=filterTagName0}"
                this.suffix = "...查看更多"
                this.suffix?.apply {
                    suffixColor(
                        "...".length,
                        this.length,
                        R.color.md_blue_500,
                        listener = View.OnClickListener { view ->
                            toast("click ${this}")
                            if (this@wrapper.isCollapsed) {
                                this@wrapper.expand()
                            }
                        })
                }
                this.transition?.duration = 5000
                sceneRoot = this.textView.parent.parent.parent as ViewGroup
                collapse(false)
                this.textView.setOnClickListener {
                    toast("click view")
                }
            }

        findViewById<View>(R.id.toggleButton).setOnClickListener {
            textViewSuffixWrapper.toggle()
        }
    }

}
