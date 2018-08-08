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

package io.plaidapp.dribbble.domain

import android.net.Uri
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.dribbble.testUiModel
import io.plaidapp.dribbble.ui.shot.ShotUiModel
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [GetShareShotInfoUseCase], mocking out its dependencies.
 */
class GetShareShotInfoUseCaseTest {

    private val imageUriProvider: ImageUriProvider = mock()
    private val uri: Uri = mock()
    private val getShareShotInfoUseCase = GetShareShotInfoUseCase(imageUriProvider)

    @Test
    fun getShareInfo_Png() = runBlocking {
        // Given a shot with a png image
        val uiModel =
            withUrl("https://cdn.dribbble.com/users/6295/screenshots/2344334/plaid_dribbble.png")

        // When invoking the use case
        val shareInfo = getShareShotInfoUseCase(uiModel)

        // Then the expected share info is returned
        assertNotNull(shareInfo)
        assertEquals(uiModel.title, shareInfo.title)
        assertFalse(shareInfo.shareText.isBlank())
        assertTrue(shareInfo.shareText.contains(uiModel.title))
        assertTrue(shareInfo.shareText.contains(uiModel.userName))
        assertTrue(shareInfo.shareText.contains(uiModel.url))
        assertTrue(shareInfo.mimeType.contains("png"))
    }

    @Test
    fun getShareInfo_Gif() = runBlocking {
        // Given a shot with a gif image
        val uiModel =
            withUrl("https://cdn.dribbble.com/users/213811/screenshots/2916762/password_visibility_toggle.gif")

        // When invoking the use case
        val shareInfo = getShareShotInfoUseCase(uiModel)

        // Then the expected share info is returned
        assertNotNull(shareInfo)
        assertEquals(uiModel.title, shareInfo.title)
        assertFalse(shareInfo.shareText.isBlank())
        assertTrue(shareInfo.shareText.contains(uiModel.title))
        assertTrue(shareInfo.shareText.contains(uiModel.userName))
        assertTrue(shareInfo.shareText.contains(uiModel.url))
        assertTrue(shareInfo.mimeType.contains("gif"))
    }

    @Test
    fun getShareInfo_Jpeg() = runBlocking {
        // Given a shot with a jpg image
        val uiModel = withUrl("https://cdn.dribbble.com/users/3557/screenshots/1550672/full2.jpg")

        // When invoking the use case
        val shareInfo = getShareShotInfoUseCase(uiModel)

        // Then the expected share info is returned
        assertNotNull(shareInfo)
        assertEquals(uiModel.title, shareInfo.title)
        assertFalse(shareInfo.shareText.isBlank())
        assertTrue(shareInfo.shareText.contains(uiModel.title))
        assertTrue(shareInfo.shareText.contains(uiModel.userName))
        assertTrue(shareInfo.shareText.contains(uiModel.url))
        assertTrue(shareInfo.mimeType.contains("jpeg"))
    }

    private fun withUrl(url: String): ShotUiModel {
        val uiModel = testUiModel.copy(imageUrl = url)
        whenever(imageUriProvider(any(), any(), any())).thenReturn(CompletableDeferred(uri))
        return uiModel
    }
}
