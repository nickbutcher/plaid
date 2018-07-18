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

package io.plaidapp.dribbble.ui.shot

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.util.event.Event
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import io.plaidapp.dribbble.domain.ShareShotInfo
import io.plaidapp.dribbble.shot
import io.plaidapp.test.shared.LiveDataTestUtil
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [DribbbleShotViewModel], mocking out its dependencies.
 */
class DribbbleShotViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val getShareShotInfoUseCase: GetShareShotInfoUseCase = mock()
    private val viewModel = DribbbleShotViewModel(
        provideFakeCoroutinesContextProvider(),
        getShareShotInfoUseCase
    )

    @Test
    fun shotClicked_sendsOpenLinkEvent() {
        // Given a view model with a shot with a given url
        val url = "https://dribbble.com/shots/2344334-Plaid-Product-Icon"
        viewModel.shot = shot.copy(htmlUrl = url)

        // When there is a request to view the shot
        viewModel.viewShotRequested()

        // Then an event is emitted to open the given url
        val openLinkEvent: Event<String>? = LiveDataTestUtil.getValue(viewModel.openLink)
        assertNotNull(openLinkEvent)
        assertEquals(url, openLinkEvent!!.peek())
    }

    @Test
    fun shotShareClicked_sendsShareInfoEvent() {
        // Given a VM with a mocked use case which return a known Share Info object
        viewModel.shot = shot
        val expected = ShareShotInfo(mock(), "Title", "Share Text", "Mime")
        whenever(getShareShotInfoUseCase.invoke(any())).thenReturn(expected)

        // When there is a request to share the shot
        viewModel.shareShotRequested()

        // Then an event is raised with the expected info
        val shareInfoEvent: Event<ShareShotInfo>? = LiveDataTestUtil.getValue(viewModel.shareShot)
        assertNotNull(shareInfoEvent)
        assertEquals(expected, shareInfoEvent!!.peek())
    }
}
