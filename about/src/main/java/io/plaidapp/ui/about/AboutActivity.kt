/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.ui.about

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.transition.TransitionInflater
import io.plaidapp.about.R
import io.plaidapp.ui.about.widget.InkPageIndicator
import io.plaidapp.core.ui.widget.ElasticDragDismissFrameLayout

/**
 * About screen. This displays 3 pages in a ViewPager:
 * – About Plaid
 * – Credit Roman for the awesome icon
 * – Credit libraries
 */
class AboutActivity : Activity() {

    private var draggableFrame: ElasticDragDismissFrameLayout? = null
    private var pager: ViewPager? = null
    private var pageIndicator: InkPageIndicator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        draggableFrame = findViewById(R.id.draggable_frame)
        pager = findViewById(R.id.pager)
        pageIndicator = findViewById(R.id.indicator)

        pager?.apply {
            adapter = AboutPagerAdapter(this@AboutActivity)
            pageMargin = resources.getDimensionPixelSize(io.plaidapp.R.dimen.spacing_normal)
        }
        pageIndicator?.setViewPager(pager)

        draggableFrame?.addListener(
                object : ElasticDragDismissFrameLayout.SystemChromeFader(this) {
                    override fun onDragDismissed() {
                        // if we drag dismiss downward then the default reversal of the enter
                        // transition would slide content upward which looks weird. So reverse it.
                        if (draggableFrame!!.translationY > 0) {
                            window.returnTransition = TransitionInflater.from(this@AboutActivity)
                                    .inflateTransition(R.transition.about_return_downward)
                        }
                        finishAfterTransition()
                    }
                })
    }
}
