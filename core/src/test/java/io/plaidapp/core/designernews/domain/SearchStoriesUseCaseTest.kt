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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.LoadSourceCallback
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.Date
import java.util.GregorianCalendar

/**
 * Tests for [SearchStoriesUseCase] mocking the dependencies.
 */
class SearchStoriesUseCaseTest {
    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val storyResponse =
        StoryResponse(id = 45L, title = "Plaid 2.0 was released", created_at = createdDate)
    private val storySequelResponse =
        StoryResponse(id = 876L, title = "Plaid 2.0 is bug free", created_at = createdDate)
    private val story =
        Story(id = 45L, title = "Plaid 2.0 was released", createdAt = createdDate)
    private val storySequel =
        Story(id = 876L, title = "Plaid 2.0 is bug free", createdAt = createdDate)
    private val storiesResponses = listOf(storyResponse, storySequelResponse)
    private val stories = listOf(story, storySequel)
    private val query = "Plaid 2.0"

    private val storiesRepository: StoriesRepository = mock()
    private val searchStoriesUseCase = SearchStoriesUseCase(
        storiesRepository,
        provideFakeCoroutinesContextProvider()
    )

    @Test
    fun search_withSuccess() = runBlocking {
        // Given a list of stories returned for a specific query and page
        val result = Result.Success(storiesResponses)
        whenever(storiesRepository.search(query, 1)).thenReturn(result)
        var sourceLoaded = false
        // Given a callback where we check the validity of the data received
        val callback = object : LoadSourceCallback {
            override fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String) {
                // Then the correct data is received
                sourceLoaded = true
                assertEquals(stories, result)
                assertEquals(1, page)
                assertEquals(query, source)
            }

            override fun loadFailed(source: String) {
                fail("Search shouldn't have failed")
            }
        }

        // When searching for stories
        searchStoriesUseCase(query, 1, callback)

        // The correct callback was called
        assertTrue(sourceLoaded)
    }

    @Test
    fun search_withError() = runBlocking {
        // Given that an error is returned for a specific query and page search
        val result = Result.Error(IOException("error"))
        whenever(storiesRepository.search(query, 2)).thenReturn(result)
        var sourceLoadingFailed = false
        // Given a callback where we check the validity of the data received
        val callback = object : LoadSourceCallback {
            override fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String) {
                fail("Search shouldn't have succeeded")
            }

            override fun loadFailed(source: String) {
                // Then the fail callback gets called for the correct source
                sourceLoadingFailed = true
                assertEquals(query, source)
            }
        }

        // When searching for stories
        searchStoriesUseCase(query, 2, callback)

        // The correct callback was called
        assertTrue(sourceLoadingFailed)
    }
}
