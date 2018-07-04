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
import io.plaidapp.core.dribbble.data.search.DribbbleSearchService
import io.plaidapp.core.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.CompletableDeferred
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response

class DribbbleRepositoryTest {

    private val service = Mockito.mock(DribbbleSearchService::class.java)
    private val dataSource = DribbbleSearchRemoteDataSource(service)
    private val repository = DribbbleRepository(dataSource, provideFakeCoroutinesContextProvider())
    private val query = "Plaid shirts"
    private val page = 0

    @Test
    fun search_whenRequestSuccessful() {
        // Given that the service responds with success
        val apiResult = Response.success(shots)
        Mockito.`when`(service.searchDeferred(query, page)).thenReturn(CompletableDeferred(apiResult))
        var result: Result<List<Shot>>? = null

        // When searching for a query
        repository.search(query, page) { result = it }

        // Then the correct API method was called
        Mockito.verify(service).searchDeferred(query, page)
        // And the expected result was returned to the callback
        assertEquals(Result.Success(shots), result)
    }

    @Test
    fun search_whenRequestFailed() {
        // Given that the service responds with failure
        val apiResult = Response.error<List<Shot>>(400, ResponseBody.create(MediaType.parse(""), "Error"))
        Mockito.`when`(service.searchDeferred(query, page)).thenReturn(CompletableDeferred(apiResult))
        var result: Result<List<Shot>>? = null

        // When searching for a query
        repository.search(query, page) { result = it }

        // Then an error result is reported
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }
}
