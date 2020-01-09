/*
 * Copyright 2019 Google LLC.
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

import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.util.HtmlParser
import io.plaidapp.dribbble.testShot
import io.plaidapp.dribbble.ui.shot.ShotStyler
import io.plaidapp.test.shared.provideFakeCoroutinesDispatcherProvider
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for [CreateShotUiModelUseCase] mocking all dependencies.
 */
@ExperimentalCoroutinesApi
class CreateShotUiModelUseCaseTest {

    private val mockHtmlParser: HtmlParser = mock()
    private val mockShotStyler: ShotStyler = mock {
        on { linkColors } doReturn ColorStateList(arrayOf(intArrayOf(1)), intArrayOf(0xff00ff))
        on { highlightColor } doReturn 0xff00ff
        on { locale } doReturn Locale.US
    }

    private val createShotUiModel =
        CreateShotUiModelUseCase(mockHtmlParser, mockShotStyler, provideFakeCoroutinesDispatcherProvider())

    @Test
    fun createModel_copiesFieldsFromShot() = runBlocking {
        // Given that we have a shot with description
        givenParsedHtml(testShot.description)

        // When creating a shotUiModel out of it
        val result = createShotUiModel(testShot)

        // Then all non-modified fields are properly mapped
        assertEquals(testShot.id, result.id)
        assertEquals(testShot.title, result.title)
        assertEquals(testShot.htmlUrl, result.url)
        assertEquals(testShot.likesCount, result.likesCount)
        assertEquals(testShot.viewsCount, result.viewsCount)
        assertEquals(testShot.createdAt, result.createdAt)
        assertEquals(testShot.images.best(), result.imageUrl)
        assertEquals(testShot.images.bestSize(), result.imageSize)
        assertEquals(testShot.user.name.toLowerCase(), result.userName)
        assertEquals(testShot.user.highQualityAvatarUrl, result.userAvatarUrl)
    }

    @Test
    fun createModel_formatsDescription() = runBlocking {
        // Given that we have a shot with description
        givenParsedHtml(testShot.description)

        // When creating a shotUiModel out of it
        val result = createShotUiModel(testShot)

        // Then the original description has been formatted and it is visible
        assertTrue(result.formattedDescription.isNotEmpty())
        assertFalse(result.shouldHideDescription)
    }

    @Test
    fun createModelWithoutDescription_hidesDescription() = runBlocking {
        // Given that we have a shot without description
        val noDescriptionShot = testShot.copy(
            description = ""
        )
        givenParsedHtml(noDescriptionShot.description)

        // When creating a shotUiModel out of it
        val result = createShotUiModel(noDescriptionShot)

        // Then the description is not visible and it is empty
        assertTrue(result.formattedDescription.isEmpty())
        assertTrue(result.shouldHideDescription)
    }

    @Test
    fun createModel_formatsViewCount() = runBlocking {
        // Given that we have a shot with description
        givenParsedHtml(testShot.description)

        // When creating a shotUiModel out of it
        val result = createShotUiModel(testShot)

        // Then the views count has been properly formatted
        assertEquals("1,234", result.formattedViewsCount)
    }

    @Test
    fun createModel_formatsLikesCount() = runBlocking {
        // Given that we have a shot with description
        givenParsedHtml(testShot.description)

        // When creating a shotUiModel out of it
        val result = createShotUiModel(testShot)

        // Then the likes count has been properly formatted
        assertEquals("5,678", result.formattedLikesCount)
    }

    /**
     * Sets up the HtmlParser mock to return the length of the passed String object
     */
    private fun givenParsedHtml(parsedString: String) {
        val mockSpannableStringBuilder: SpannableStringBuilder = mock {
            on { length } doReturn parsedString.length
        }
        whenever(mockHtmlParser.parse(any(), any(), any())).thenReturn(mockSpannableStringBuilder)
    }
}
