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

package io.plaidapp.core.dribbble.data

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Tests for [ShotsRepository] which mocks all dependencies.
 */
class ShotsRepositoryTest {

    private val dataSource: SearchRemoteDataSource = mock()
    private val repository = ShotsRepository(
        dataSource,
        provideFakeCoroutinesContextProvider()
    )
    private val query = "Plaid shirts"
    private val page = 0

    @Test
    fun search_whenRequestSuccessful() = runBlocking {
        // Given that the data source responds with success
        val result = Result.Success(shots)
        whenever(dataSource.search(query, page)).thenReturn(result)
        var data: Result<List<Shot>>? = null

        // When searching for a query
        repository.search(query, page) { data = it }

        // Then the correct method was called
        verify(dataSource).search(query, page)
        // And the expected result was returned to the callback
        assertEquals(Result.Success(shots), data)
    }

    @Test
    fun search_whenRequestFailed() = runBlocking {
        // Given that the data source responds with failure
        val result = Result.Error(IOException("error"))
        whenever(dataSource.search(query, page)).thenReturn(result)
        var data: Result<List<Shot>>? = null

        // When searching for a query
        repository.search(query, page) { data = it }

        // Then an error result is reported
        assertNotNull(data)
        assertTrue(data is Result.Error)
    }

    @Test
    fun getShot_whenSearchSucceeded() = runBlocking {
        // Given that a search has been performed successfully and data cached
        whenever(dataSource.search(query, page)).thenReturn(Result.Success(shots))
        repository.search(query, page) { }

        // When getting a shot by id
        val result = repository.getShot(shots[0].id)

        // Then it is successfully retrieved
        assertNotNull(result)
        assertTrue(result is Result.Success)
        assertEquals(shots[0], (result as Result.Success).data)
    }

    @Test
    fun getShot_whenSearchFailed() = runBlocking {
        // Given that a search fails so no data is cached
        whenever(dataSource.search(query, page)).thenReturn(Result.Error(IOException("error")))
        repository.search(query, page) { }

        // When getting a shot by id
        val result = repository.getShot(shots[0].id)

        // Then an Error is reported
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }
}
