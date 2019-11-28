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

package io.plaidapp.core.producthunt.data

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.producthunt.data.api.ProductHuntService
import io.plaidapp.core.producthunt.data.api.model.GetPostsResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests for [ProductHuntRemoteDataSource] that mock the Product Hunt API
 */
class ProductHuntRemoteDataSourceTest {

    private val service: ProductHuntService = mock()
    private val dataSource = ProductHuntRemoteDataSource(service)

    @Test
    fun loadData_whenResultSuccessful() = runBlocking {
        // Given that the service responds with success
        val result = Response.success(responseDataSuccess)
        whenever(service.getPostsAsync(1)).thenReturn(result)

        // When loading the data
        val response = dataSource.loadData(1)

        // Then the response is the expected one
        assertNotNull(response)
        assertEquals(Result.Success(responseDataSuccess), response)
    }

    @Test
    fun loadData_whenRequestFailed() = runBlocking {
        // Given that the service responds with failure
        val result = Response.error<GetPostsResponse>(
            400,
            errorResponseBody
        )
        whenever(service.getPostsAsync(1)).thenReturn(result)

        // When loading posts
        val response = dataSource.loadData(1)

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }
}
