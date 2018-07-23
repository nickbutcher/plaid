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
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.res.ColorStateList
import android.content.res.Resources
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.plaidapp.about.ui.AboutStyler
import io.plaidapp.test.shared.LiveDataTestUtil
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test the [AboutViewModel]'s library click function.
 *
 * Mocking [AboutStyler], [Markdown] and [Resources].
 */
class AboutViewModelTest {

    private lateinit var aboutViewModel: AboutViewModel

    private val aboutStyler = mock<AboutStyler> {
        on { linksColor } doReturn ColorStateList(arrayOf(intArrayOf(1)), intArrayOf(0xff00ff))
        on { highlightColor } doReturn 0xff00ff
    }

    private val resources = mock<Resources> { on { getString(any()) } doReturn "Mock resources" }
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

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        aboutViewModel = AboutViewModel(aboutStyler, resources, markdown)
    }

    @Test
    fun onLibraryClick() {
        // Click on all the libraries
        aboutViewModel.libraries.forEach {
            aboutViewModel.onLibraryClick(it)
            val event = LiveDataTestUtil.getValue(aboutViewModel.navigationTarget)
            assertThat(event?.peek(), `is`(equalTo(it.link)))
        }
    }
}
