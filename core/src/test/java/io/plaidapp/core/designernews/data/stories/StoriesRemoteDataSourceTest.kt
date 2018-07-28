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

package io.plaidapp.core.designernews.data.stories

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.core.designernews.errorResponseBody
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.Date
import java.util.GregorianCalendar

/**
 * Test for [StoriesRemoteDataSource] mocking all dependencies.
 */
class StoriesRemoteDataSourceTest {

    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val story =
        StoryResponse(id = 45L, title = "Plaid 2.0 was released", created_at = createdDate)
    private val storySequel =
        StoryResponse(id = 876L, title = "Plaid 2.0 is bug free", created_at = createdDate)
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
    fun search_withSuccess() = runBlocking {
        // Given that the service responds with success
        withSearchSuccess(query, 2, stories)

        // When searching for stories
        val result = dataSource.search(query, 2)

        // Then there's one request to the service
        verify(service).search(query, 2)
        // Then the correct list of stories is returned
        assertEquals(Result.Success(stories), result)
    }

    @Test
    fun search_withError() = runBlocking {
        // Given that the service responds with error
        withSearchError(query, 1)

        // When searching for stories
        val result = dataSource.search(query, 1)

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    private fun withStoriesSuccess(page: Int, users: List<StoryResponse>) {
        val result = Response.success(users)
        whenever(service.getStories(page)).thenReturn(CompletableDeferred(result))
    }

    private fun withStoriesError(page: Int) {
        val result = Response.error<List<StoryResponse>>(
            400,
            errorResponseBody
        )
        whenever(service.getStories(page)).thenReturn(CompletableDeferred(result))
    }

    private fun withSearchSuccess(query: String, page: Int, users: List<StoryResponse>) {
        val result = Response.success(users)
        whenever(service.search(query, page)).thenReturn(CompletableDeferred(result))
    }

    private fun withSearchError(query: String, page: Int) {
        val result = Response.error<List<StoryResponse>>(
            400,
            errorResponseBody
        )
        whenever(service.search(query, page)).thenReturn(CompletableDeferred(result))
    }
}
