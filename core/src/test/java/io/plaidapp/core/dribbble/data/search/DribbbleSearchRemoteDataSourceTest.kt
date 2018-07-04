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

package io.plaidapp.core.dribbble.data.search

import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.shots
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response

class DribbbleSearchRemoteDataSourceTest {

    private val service = Mockito.mock(DribbbleSearchService::class.java)
    private val dataSource = DribbbleSearchRemoteDataSource(service)
    private val query = "Plaid shirts"
    private val page = 0

    @Test
    fun search_whenRequestSuccessful() = runBlocking {
        // Given that the service responds with success
        val result = Response.success(shots)
        Mockito.`when`(service.searchDeferred(query, page)).thenReturn(CompletableDeferred(result))

        // When performing a search
        val response = dataSource.search(query, page)

        // Then the response is as expected
        assertNotNull(response)
        assertEquals(Result.Success(shots), response)
    }

    @Test
    fun search_whenRequestFailed() = runBlocking {
        // Given that the service responds with failure
        val result = Response.error<List<Shot>>(400, ResponseBody.create(MediaType.parse(""), "Error"))
        Mockito.`when`(service.searchDeferred(query, page)).thenReturn(CompletableDeferred(result))

        // When performing a search
        val response = dataSource.search(query, page)

        // Then an error is reported
        assertTrue(response is Result.Error)
    }

    @Test
    fun search_whenResponseEmpty() = runBlocking {
        // Given that the service responds with success but with an empty response
        val result = Response.success<List<Shot>>(null)
        Mockito.`when`(service.searchDeferred(query, page)).thenReturn(CompletableDeferred(result))

        // When performing a search
        val response = dataSource.search(query, page)

        // Then an error is reported
        assertTrue(response is Result.Error)
    }
}
