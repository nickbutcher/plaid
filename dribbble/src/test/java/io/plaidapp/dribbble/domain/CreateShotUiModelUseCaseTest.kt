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

import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.util.HtmlParser
import io.plaidapp.dribbble.testUiModel
import io.plaidapp.dribbble.ui.shot.ShotStyler
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Tests for [CreateShotUiModelUseCase], mocking out its dependencies.
 */
class CreateShotUiModelUseCaseTest {

    private val mockHtmlParser: HtmlParser = mock()
    private val mockShotStyler: ShotStyler = mock {
        on { linkColors } doReturn ColorStateList(arrayOf(intArrayOf(1)), intArrayOf(0xff00ff))
        on { highlightColor } doReturn 0xff00ff
        on { locale } doReturn Locale.US
    }
    private val createShotUiModel = CreateShotUiModelUseCase(mockHtmlParser)

    @Test
    fun createModelWithDescription_formatsDescription() = runBlocking {
        // Given a UI Model with a description but no formatted description
        val desc = "This is a description"
        val initialModel = testUiModel.copy(
            description = desc,
            formattedDescription = ""
        )
        withParsedHtml(desc)

        // When executing the use case
        val result = createShotUiModel(initialModel, mockShotStyler).await()

        // Then the formatted description is set
        assertTrue(result.formattedDescription.isNotEmpty())
        // And it should not be hidden
        assertFalse(result.hideDescription)
    }

    @Test
    fun createModelWithoutDescription_formatsDescription() = runBlocking {
        // Given a UI Model without a description & no formatted description
        val nodesc = ""
        val initialModel = testUiModel.copy(
            description = nodesc,
            formattedDescription = ""
        )
        withParsedHtml(nodesc)

        // When executing the use case
        val result = createShotUiModel(initialModel, mockShotStyler).await()

        // Then the formatted description should remain empty
        assertTrue(result.formattedDescription.isEmpty())
        // And it should be hidden
        assertTrue(result.hideDescription)
    }

    @Test
    fun createModel_formatsViewCount() = runBlocking {
        // Given a UI Model with a view count, but no formatted view count
        val initialModel = testUiModel.copy(
            viewsCount = 123456,
            formattedViewsCount = ""
        )
        withParsedHtml("")

        // When executing the use case
        val result = createShotUiModel(initialModel, mockShotStyler).await()

        // Then the expected formatted view count is set
        assertEquals("123,456", result.formattedViewsCount)
    }

    @Test
    fun createModel_formatsLikesCount() = runBlocking {
        // Given a UI Model with a likes count, but no formatted likes count
        val initialModel = testUiModel.copy(
            likesCount = 567890,
            formattedViewsCount = ""
        )
        withParsedHtml("")

        // When executing the use case
        val result = createShotUiModel(initialModel, mockShotStyler).await()

        // Then the expected formatted likes count is set
        assertEquals("567,890", result.formattedLikesCount)
    }

    private fun withParsedHtml(parsedString: String) {
        val mockSSB: SpannableStringBuilder = mock {
            on { length } doReturn parsedString.length
        }
        whenever(mockHtmlParser.invoke(any(), any(), any())).thenReturn(mockSSB)
    }
}
