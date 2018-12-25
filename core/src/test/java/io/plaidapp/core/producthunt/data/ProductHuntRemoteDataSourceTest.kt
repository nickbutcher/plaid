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

package io.plaidapp.core.producthunt.data

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.producthunt.data.api.ProductHuntService
import io.plaidapp.core.producthunt.data.api.model.Post
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests for [ProductHuntRemoteDataSource] that mock the Product Hunt API
 */
class ProductHuntRemoteDataSourceTest {

    private val errorResponseBody = ResponseBody.create(MediaType.parse(""), "Error")

    private val post1 = Post(
            id = 345L,
            title = "Plaid is plady amazing",
            url = "www.plaid.amazing",
            name = "Plaid",
            tagline = "amazing",
            discussionUrl = "www.disc.plaid",
            redirectUrl = "www.d.plaid",
            commentsCount = 5,
            votesCount = 100
    )
    private val post2 = Post(
            id = 947L,
            title = "Plaid is team amazing",
            url = "www.plaid.team",
            name = "Plaid",
            tagline = "team",
            discussionUrl = "www.team.plaid",
            redirectUrl = "www.t.plaid",
            commentsCount = 2,
            votesCount = 42
    )

    private val postsResult = listOf(post1, post2)

    private val service: ProductHuntService = mock()
    private val dataSource = ProductHuntRemoteDataSource(service)

    @Test
    fun loadData_whenResultSuccessful() = runBlocking {
        // Given that the service responds with success
        val result = Response.success(postsResult)
        whenever(service.getPosts(1)).thenReturn(CompletableDeferred(result))

        // When loading the data
        val response = dataSource.loadData(1)

        // Then the response is the expected one
        assertNotNull(response)
        assertEquals(Result.Success(postsResult), response)
    }

    @Test
    fun loadData_whenRequestFailed() = runBlocking {
        // Given that the service responds with failure
        val result = Response.error<List<Post>>(
                400,
                errorResponseBody
        )
        whenever(service.getPosts(1)).thenReturn(CompletableDeferred(result))

        // When loading posts
        val response = dataSource.loadData(1)

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }
}
