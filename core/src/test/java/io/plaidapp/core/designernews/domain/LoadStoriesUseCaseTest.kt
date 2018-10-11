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

package io.plaidapp.core.designernews.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.LoadSourceCallback
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.Result
import io.plaidapp.core.data.prefs.SourceManager
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.core.designernews.storyLinks
import io.plaidapp.core.designernews.userId
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.Date
import java.util.GregorianCalendar

/**
 * Tests for [LoadStoriesUseCase] mocking the dependencies.
 */
class LoadStoriesUseCaseTest {
    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val storyResponse = StoryResponse(
        id = 45L,
        title = "Plaid 2.0 was released",
        created_at = createdDate,
        links = storyLinks
    )
    private val storySequelResponse = StoryResponse(
        id = 876L,
        title = "Plaid 2.0 is bug free",
        created_at = createdDate,
        links = storyLinks
    )
    private val story = Story(
        id = 45L,
        title = "Plaid 2.0 was released",
        createdAt = createdDate,
        userId = userId,
        links = storyLinks
    )
    private val storySequel = Story(
        id = 876L,
        title = "Plaid 2.0 is bug free",
        createdAt = createdDate,
        userId = userId,
        links = storyLinks
    )
    private val storyResponses = listOf(storyResponse, storySequelResponse)
    private val stories = listOf(story, storySequel)

    private val storiesRepository: StoriesRepository = mock()
    private val loadStoriesUseCase = LoadStoriesUseCase(
        storiesRepository,
        provideFakeCoroutinesContextProvider()
    )

    @Test
    fun loadStories_withSuccess() = runBlocking {
        // Given a list of story responses returned for a specific page
        val result = Result.Success(storyResponses)
        whenever(storiesRepository.loadStories(1)).thenReturn(result)
        var sourceLoaded = false
        // Given a callback where we check the validity of the data received
        val callback = object : LoadSourceCallback {
            override fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String) {
                // Then the correct data is received
                sourceLoaded = true
                assertEquals(stories, result)
                assertEquals(1, page)
                assertEquals(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR, source)
            }

            override fun loadFailed(source: String) {
                fail("Load shouldn't have failed")
            }
        }

        // When loading stories
        loadStoriesUseCase(1, callback)

        // The correct callback was called
        assertTrue(sourceLoaded)
    }

    @Test
    fun loadStories_withError() = runBlocking {
        // Given that an error is returned for a specific page
        val result = Result.Error(IOException("error"))
        whenever(storiesRepository.loadStories(2)).thenReturn(result)
        var sourceLoadingFailed = false
        // Given a callback where we check the validity of the data received
        val callback = object : LoadSourceCallback {
            override fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String) {
                fail("Load shouldn't have succeeded")
            }

            override fun loadFailed(source: String) {
                // Then the fail callback gets called for the correct source
                sourceLoadingFailed = true
                assertEquals(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR, source)
            }
        }

        // When loading stories
        loadStoriesUseCase(2, callback)

        // The correct callback was called
        assertTrue(sourceLoadingFailed)
    }
}
