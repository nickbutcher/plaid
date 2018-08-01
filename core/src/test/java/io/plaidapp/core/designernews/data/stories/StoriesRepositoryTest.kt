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
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.Date
import java.util.GregorianCalendar

/**
 * Tests for [StoriesRepository] mocking all the dependencies.
 */
class StoriesRepositoryTest {
    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val story =
        StoryResponse(id = 45L, title = "Plaid 2.0 was released", created_at = createdDate)
    private val storySequel =
        StoryResponse(id = 876L, title = "Plaid 2.0 is bug free", created_at = createdDate)
    private val stories = listOf(story, storySequel)
    private val query = "Plaid 2.0"

    private val dataSource: StoriesRemoteDataSource = mock()
    private val repository = StoriesRepository(dataSource)

    @Test
    fun loadStories_withSuccess() = runBlocking {
        // Given a list of stories returned for a specific page
        val result = Result.Success(stories)
        whenever(dataSource.loadStories(1)).thenReturn(result)

        // When loading stories
        val data = repository.loadStories(1)

        // The correct data is returned
        assertEquals(Result.Success(stories), data)
    }

    @Test
    fun loadStories_withError() = runBlocking {
        // Given that an error is returned for a specific page
        val result = Result.Error(IOException("error"))
        whenever(dataSource.loadStories(2)).thenReturn(result)

        // When loading stories
        val data = repository.loadStories(2)

        // Then error is returned
        assertTrue(data is Result.Error)
    }

    @Test
    fun search_withSuccess() = runBlocking {
        // Given a list of stories returned for a specific query and page
        val result = Result.Success(stories)
        whenever(dataSource.search(query, 1)).thenReturn(result)

        // When searching for stories
        val data = repository.search(query, 1)

        // The correct data is returned
        assertEquals(Result.Success(stories), data)
    }

    @Test
    fun search_withError() = runBlocking {
        // Given that an error is returned for a specific query and page search
        val result = Result.Error(IOException("error"))
        whenever(dataSource.search(query, 2)).thenReturn(result)

        // When searching for stories
        val data = repository.search(query, 2)

        // Then error data is returned
        assertTrue(data is Result.Error)
    }

    @Test
    fun getStory_whenLoadSucceeded() = runBlocking {
        // Given that a load has been performed successfully and data cached
        whenever(dataSource.loadStories(1)).thenReturn(Result.Success(stories))
        repository.loadStories(1)

        // When getting a story by id
        val result = repository.getStory(stories[0].id)

        // Then it is successfully retrieved
        assertNotNull(result)
        assertTrue(result is Result.Success)
        assertEquals(stories[0], (result as Result.Success).data)
    }

    @Test
    fun getStory_whenLoadFailed() = runBlocking {
        // Given that a search fails so no data is cached
        whenever(dataSource.loadStories(1)).thenReturn(Result.Error(IOException("error")))
        repository.loadStories(1)

        // When getting a story by id
        val result = repository.getStory(stories[0].id)

        // Then error is returned
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }

    @Test
    fun getStory_whenSearchSucceeded() = runBlocking {
        // Given that a search has been performed successfully and data cached
        whenever(dataSource.search(query, 1)).thenReturn(Result.Success(stories))
        repository.search(query, 1)

        // When getting a story by id
        val result = repository.getStory(stories[0].id)

        // Then it is successfully retrieved
        assertNotNull(result)
        assertTrue(result is Result.Success)
        assertEquals(stories[0], (result as Result.Success).data)
    }

    @Test
    fun getStory_whenSearchFailed() = runBlocking {
        // Given that a search fails so no data is cached
        whenever(dataSource.search(query, 1)).thenReturn(Result.Error(IOException("error")))
        repository.search(query, 1)

        // When getting a story by id
        val result = repository.getStory(stories[0].id)

        // Then error is returned
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }
}
