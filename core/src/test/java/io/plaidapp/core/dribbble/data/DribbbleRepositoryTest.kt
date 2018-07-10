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

import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.search.DribbbleSearchRemoteDataSource
import io.plaidapp.core.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import java.io.IOException

/**
 * Tests for [DribbbleRepository] which mocks all dependencies.
 */
class DribbbleRepositoryTest {

    private val dataSource = Mockito.mock(DribbbleSearchRemoteDataSource::class.java)
    private val repository = DribbbleRepository(dataSource, provideFakeCoroutinesContextProvider())
    private val query = "Plaid shirts"
    private val page = 0

    @Test
    fun search_whenRequestSuccessful() = runBlocking {
        // Given that the data source responds with success
        val result = Result.Success(shots)
        Mockito.`when`(dataSource.search(query, page)).thenReturn(result)
        var data: Result<List<Shot>>? = null

        // When searching for a query
        repository.search(query, page) { data = it }

        // Then the correct method was called
        Mockito.verify(dataSource).search(query, page)
        // And the expected result was returned to the callback
        assertEquals(Result.Success(shots), data)
    }

    @Test
    fun search_whenRequestFailed() = runBlocking {
        // Given that the data source responds with failure
        val result = Result.Error(IOException("error"))
        Mockito.`when`(dataSource.search(query, page)).thenReturn(result)
        var data: Result<List<Shot>>? = null

        // When searching for a query
        repository.search(query, page) { data = it }

        // Then an error result is reported
        assertNotNull(data)
        assertTrue(data is Result.Error)
    }
}
