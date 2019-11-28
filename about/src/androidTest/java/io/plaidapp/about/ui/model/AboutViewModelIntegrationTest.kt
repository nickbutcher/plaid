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

package io.plaidapp.about.ui.model

import `in`.uncod.android.bypass.Bypass
import `in`.uncod.android.bypass.Markdown
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import io.plaidapp.about.ui.AboutActivity
import io.plaidapp.about.ui.AboutStyler
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test the behavior of [AboutViewModel].
 *
 * Mock [Markdown] due to native dependency as well as [Resources].
 */
@RunWith(AndroidJUnit4::class)
class AboutViewModelIntegrationTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var activityTestRule = ActivityTestRule(AboutActivity::class.java)

    private lateinit var markdown: Markdown
    private lateinit var aboutStyler: AboutStyler
    private lateinit var aboutViewModel: AboutViewModel

    @Before fun setUpViewModel() {
        val activity = activityTestRule.activity
        val resources = activity.resources

        aboutStyler = AboutStyler(activity)
        markdown = Bypass(resources.displayMetrics, Bypass.Options())
        aboutViewModel = AboutViewModel(aboutStyler, resources, markdown)
    }

    @Test fun testLibraryClick() {
        aboutViewModel.libraries.forEach {
            aboutViewModel.onLibraryClick(it)
            assertEquals(aboutViewModel.navigationTarget.value!!.peek(), it.link)
        }
    }
}
