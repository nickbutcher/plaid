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

package io.plaidapp.about.ui.model

import `in`.uncod.android.bypass.Markdown
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.res.ColorStateList
import android.content.res.Resources
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.plaidapp.about.ui.AboutStyler
import org.junit.Before
import org.junit.Rule

/**
 * Test the behavior of [AboutViewModel].
 *
 * Mocking [Markdown] due to native dependency as well as [Resources].
 */
class AboutViewModelIntegrationTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val markdown = mock<Markdown> {
        on {
            markdownToSpannable(
                any(),
                any(),
                any(),
                anyOrNull()
            )
        } doReturn "Mock markdown"
    }
    private val resources = mock<Resources> { on { getString(any()) } doReturn "Mock resources" }

    private val aboutStyler = mock<AboutStyler> {
        on { linksColor } doReturn ColorStateList(arrayOf(intArrayOf(0, 1)), intArrayOf(0xff00ff))
        on { highlightColor } doReturn 0xff00ff
    }

    private lateinit var aboutViewModel: AboutViewModel

    @Before
    fun setUpViewModel() {
        aboutViewModel = AboutViewModel(aboutStyler, resources, markdown)
    }
}
