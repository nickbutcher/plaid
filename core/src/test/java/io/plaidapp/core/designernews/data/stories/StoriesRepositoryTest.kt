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
import io.plaidapp.core.data.LoadSourceCallback
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.Result
import io.plaidapp.core.data.prefs.SourceManager
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.Date
import java.util.GregorianCalendar

/**
 * Tests for [StoriesRepository] mocking all the dependencies.
 */
class StoriesRepositoryTest {
    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val story = Story(id = 45L, title = "Plaid 2.0 was released", createdAt = createdDate)
    private val storySequel =
        Story(id = 876L, title = "Plaid 2.0 is bug free", createdAt = createdDate)
    private val stories = listOf(story, storySequel)
    private val query = "Plaid 2.0"
    private val emptyCallback = object : LoadSourceCallback {
        override fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String) {
        }

        override fun loadFailed(source: String) {
        }
    }

    private val dataSource: StoriesRemoteDataSource = mock()
    private val repository = StoriesRepository(dataSource, provideFakeCoroutinesContextProvider())

    @Test
    fun loadStories_withSuccess() = runBlocking {
        // Given a list of stories returned for a specific page
        val result = Result.Success(stories)
        whenever(dataSource.loadStories(1)).thenReturn(result)
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
        repository.loadStories(1, callback)

        // The correct callback was called
        assertTrue(sourceLoaded)
    }

    @Test
    fun loadStories_withError() = runBlocking {
        // Given that an error is returned for a specific page
        val result = Result.Error(IOException("error"))
        whenever(dataSource.loadStories(2)).thenReturn(result)
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
        repository.loadStories(2, callback)

        // The correct callback was called
        assertTrue(sourceLoadingFailed)
    }

    @Test
    fun search_withSuccess() = runBlocking {
        // Given a list of stories returned for a specific query and page
        val result = Result.Success(stories)
        whenever(dataSource.search(query, 1)).thenReturn(result)
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
        repository.search(query, 1, callback)

        // The correct callback was called
        assertTrue(sourceLoaded)
    }

    @Test
    fun search_withError() = runBlocking {
        // Given that an error is returned for a specific query and page search
        val result = Result.Error(IOException("error"))
        whenever(dataSource.search(query, 2)).thenReturn(result)
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
        repository.search(query, 2, callback)

        // The correct callback was called
        assertTrue(sourceLoadingFailed)
    }

    @Test
    fun getStory_whenLoadSucceeded() = runBlocking {
        // Given that a load has been performed successfully and data cached
        whenever(dataSource.loadStories(1)).thenReturn(Result.Success(stories))
        repository.loadStories(1, emptyCallback)

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
        repository.loadStories(1, emptyCallback)

        // When getting a story by id
        val result = repository.getStory(stories[0].id)

        // Then an Error is reported
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }

    @Test
    fun getStory_whenSearchSucceeded() = runBlocking {
        // Given that a search has been performed successfully and data cached
        whenever(dataSource.search(query, 1)).thenReturn(Result.Success(stories))
        repository.search(query, 1, emptyCallback)

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
        repository.search(query, 1, emptyCallback)

        // When getting a story by id
        val result = repository.getStory(stories[0].id)

        // Then an Error is reported
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }
}
