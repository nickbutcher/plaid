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

package io.plaidapp.core.designernews.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.core.designernews.storyLinks
import io.plaidapp.core.designernews.userId
import java.io.IOException
import java.util.Date
import java.util.GregorianCalendar
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [SearchStoriesUseCase] mocking the dependencies.
 */
class SearchStoriesUseCaseTest {
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
        page = 1,
        createdAt = createdDate,
        userId = userId,
        links = storyLinks
    )
    private val storySequel = Story(
        id = 876L,
        title = "Plaid 2.0 is bug free",
        page = 1,
        createdAt = createdDate,
        userId = userId,
        links = storyLinks
    )
    private val storiesResponses = listOf(storyResponse, storySequelResponse)
    private val stories = listOf(story, storySequel)
    private val query = "Plaid 2.0"

    private val storiesRepository: StoriesRepository = mock()
    private val searchStoriesUseCase = SearchStoriesUseCase(storiesRepository)

    @Test
    fun search_withSuccess() = runBlocking {
        // Given a list of stories returned for a specific query and page
        val expected = Result.Success(storiesResponses)
        whenever(storiesRepository.search(query, 1)).thenReturn(expected)

        // When searching for stories
        val result = searchStoriesUseCase(query, 1)

        // Then the result was triggered
        assertEquals(Result.Success(stories), result)
    }

    @Test
    fun search_withError() = runBlocking {
        // Given that an error is returned for a specific query and page search
        val expected = Result.Error(IOException("error"))
        whenever(storiesRepository.search(query, 2)).thenReturn(expected)

        // When searching for stories
        val result = searchStoriesUseCase(query, 2)

        // Then the result was triggered
        assertEquals(expected, result)
    }
}
