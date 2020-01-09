/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.about.ui

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import io.plaidapp.R as appR
import io.plaidapp.about.R
import io.plaidapp.about.dagger.inject
import io.plaidapp.about.databinding.ActivityAboutBinding
import io.plaidapp.about.ui.adapter.AboutPagerAdapter
import io.plaidapp.about.ui.model.AboutViewModel
import io.plaidapp.about.ui.model.AboutViewModelFactory
import io.plaidapp.core.ui.widget.ElasticDragDismissFrameLayout
import io.plaidapp.core.util.customtabs.CustomTabActivityHelper
import io.plaidapp.core.util.delegates.contentView
import io.plaidapp.core.util.event.EventObserver
import javax.inject.Inject

/**
 * About screen. This displays 3 pages in a ViewPager:
 * – About Plaid
 * – Credit Roman for the awesome icon
 * – Credit libraries
 */
class AboutActivity : AppCompatActivity() {

    @Inject
    internal lateinit var aboutViewModelFactory: AboutViewModelFactory

    private val binding by contentView<AboutActivity, ActivityAboutBinding>(
        R.layout.activity_about
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        inject()

        val viewModel = ViewModelProvider(this, aboutViewModelFactory)
            .get(AboutViewModel::class.java)
            .apply {
                navigationTarget.observe(this@AboutActivity, EventObserver { url ->
                    openLink(url)
                })
            }

        binding.pager.apply {
            adapter = AboutPagerAdapter(viewModel.uiModel)

            // Set the margin between pages in the ViewPager2
            val pageMargin = resources.getDimensionPixelSize(appR.dimen.spacing_normal)
            setPageTransformer { page, position -> page.translationX = position * pageMargin }
        }

        binding.indicator.setViewPager(binding.pager)

        binding.draggableFrame.addListener(
            object : ElasticDragDismissFrameLayout.SystemChromeFader(this) {
                override fun onDragDismissed() {
                    // if we drag dismiss downward then the default reversal of the enter
                    // transition would slide content upward which looks weird. So reverse it.
                    if (binding.draggableFrame.translationY > 0) {
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
