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

import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionInflater
import androidx.core.net.toUri
import io.plaidapp.about.R
import io.plaidapp.core.ui.widget.ElasticDragDismissFrameLayout
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.customtabs.CustomTabActivityHelper
import io.plaidapp.core.util.event.EventObserver
import io.plaidapp.ui.about.uimodel.AboutUiModel
import io.plaidapp.ui.about.uimodel.LibrariesUiModel
import io.plaidapp.ui.about.widget.InkPageIndicator
import io.plaidapp.R as appR

/**
 * About screen. This displays 3 pages in a ViewPager:
 * – About Plaid
 * – Credit Roman for the awesome icon
 * – Credit libraries
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val draggableFrame = findViewById<ElasticDragDismissFrameLayout>(R.id.draggable_frame)
        val pager = findViewById<ViewPager>(R.id.pager)
        val pageIndicator = findViewById<InkPageIndicator>(R.id.indicator)

        val linksColor = ContextCompat.getColorStateList(application,
                appR.color.plaid_links)!!
        val highlightColor = ColorUtils.getThemeColor(application,
                appR.attr.colorPrimary, appR.color.primary)
        val aboutStyler = AboutStyler(linksColor, highlightColor)

        val viewModel = AboutViewModel(aboutStyler, resources).apply {
            navigationTarget.observe(this@AboutActivity, EventObserver { url ->
                openLink(url)
            })
        }

        val uiModel = with(viewModel) {
            AboutUiModel(
                    appAboutText,
                    iconAboutText,
                    LibrariesUiModel(libraries) {
                        onLibraryClick(it)
                    })
        }

        pager.apply {
            adapter = AboutPagerAdapter(uiModel)
            pageMargin = resources.getDimensionPixelSize(appR.dimen.spacing_normal)
        }

        pageIndicator?.setViewPager(pager)

        draggableFrame?.addListener(
            object : ElasticDragDismissFrameLayout.SystemChromeFader(this) {
                override fun onDragDismissed() {
                    // if we drag dismiss downward then the default reversal of the enter
                    // transition would slide content upward which looks weird. So reverse it.
                    if (draggableFrame.translationY > 0) {
                        window.returnTransition = TransitionInflater.from(this@AboutActivity)
                            .inflateTransition(R.transition.about_return_downward)
                    }
                    finishAfterTransition()
                }
            })
    }

    private fun openLink(link: String) {
        CustomTabActivityHelper.openCustomTab(
            this,
            CustomTabsIntent.Builder()
                .setToolbarColor(
                    ContextCompat.getColor(
                        this,
                        appR.color.primary
                    )
                )
                .addDefaultShareMenuItem()
                .build(),
            link.toUri()
        )
    }
}
