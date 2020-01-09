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

package io.plaidapp.core.designernews.data.stories

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.core.designernews.errorResponseBody
import io.plaidapp.core.designernews.storyLinks
import java.net.UnknownHostException
import java.util.Date
import java.util.GregorianCalendar
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Test for [StoriesRemoteDataSource] mocking all dependencies.
 */
class StoriesRemoteDataSourceTest {

    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val story = StoryResponse(
        id = 45L,
        title = "Plaid 2.0 was released",
        created_at = createdDate,
        links = storyLinks
    )
    private val storySequel = StoryResponse(
        id = 876L,
        title = "Plaid 2.0 is bug free",
        created_at = createdDate,
        links = storyLinks
    )
    private val stories = listOf(story, storySequel)
    private val query = "Plaid 2.0"

    private val service: DesignerNewsService = mock()
    private val dataSource = StoriesRemoteDataSource(service)

    @Test
    fun loadStories_withSuccess() = runBlocking {
        // Given that the service responds with success
        withStoriesSuccess(2, stories)

        // When requesting stories
        val result = dataSource.loadStories(2)

        // Then there's one request to the service
        verify(service).getStories(2)
        // Then the correct list of stories is returned
        assertEquals(Result.Success(stories), result)
    }

    @Test
    fun loadStories_withError() = runBlocking {
        // Given that the service responds with error
        withStoriesError(1)

        // When requesting stories
        val result = dataSource.loadStories(1)

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    @Test
    fun loadStories_withException() = runBlocking {
        // Given that the service throws an exception
        doAnswer { throw UnknownHostException() }
            .whenever(service).getStories(1)

        // When requesting stories
        val result = dataSource.loadStories(1)

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    @Test
    fun search_withSuccess() = runBlocking {
        // Given that the service responds with success
        val storyIds = stories.map { it.id.toString() }
        whenever(service.search(query, 2)).thenReturn(Response.success(storyIds))
        val commaSeparatedIds = storyIds.joinToString(",")
        whenever(service.getStories(commaSeparatedIds)).thenReturn(Response.success(stories))

        // When searching for stories
        val result = dataSource.search(query, 2)

        // Then the correct list of stories is returned
        assertEquals(Result.Success(stories), result)
    }

    @Test
    fun search_withErrorScrapingResults() = runBlocking {
        // Given that the service responds with error
        val error = Response.error<List<String>>(400, errorResponseBody)
        whenever(service.search(query, 1)).thenReturn(error)

        // When searching for stories
        val result = dataSource.search(query, 1)

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    @Test
    fun search_withExceptionScrapingResults() = runBlocking {
        // Given that the service throws an exception
        doAnswer { throw UnknownHostException() }
            .whenever(service).search(query, 1)

        // When searching for stories
        val result = dataSource.search(query, 1)

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    @Test
    fun search_withErrorFetchingStories() = runBlocking {
        // Given that the service responds with error
        val storyIds = stories.joinToString(",") { it.id.toString() }
        val error = Response.error<List<StoryResponse>>(400, errorResponseBody)
        whenever(service.getStories(storyIds)).thenReturn(error)

        // When searching for stories
        val result = dataSource.search(query, 1)

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    @Test
    fun search_withExceptionFetchingStories() = runBlocking {
        // Given that the service throws an exception
        doAnswer { throw UnknownHostException() }
            .whenever(service).getStories(stories.joinToString(",") { it.id.toString() })

        // When searching for stories
        val result = dataSource.search(query, 1)

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    private suspend fun withStoriesSuccess(page: Int, stories: List<StoryResponse>) {
        val result = Response.success(stories)
        whenever(service.getStories(page)).thenReturn(result)
    }

    private suspend fun withStoriesError(page: Int) {
        val result = Response.error<List<StoryResponse>>(
            400,
            errorResponseBody
        )
        whenever(service.getStories(page)).thenReturn(result)
    }
}
