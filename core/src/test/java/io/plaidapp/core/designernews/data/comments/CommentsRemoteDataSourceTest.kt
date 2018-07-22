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

package io.plaidapp.core.designernews.data.comments

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.errorResponseBody
import io.plaidapp.core.designernews.data.comments.model.CommentResponse
import io.plaidapp.core.designernews.repliesResponses
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response

/**
 * Tests for [CommentsRemoteDataSource] that mock the Designer News API
 */
class CommentsRemoteDataSourceTest {

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val dataSource = CommentsRemoteDataSource(service)

    @Test
    fun getComments_whenRequestSuccessful() = runBlocking {
        // Given that the service responds with success
        val result = Response.success(repliesResponses)
        Mockito.`when`(service.getComments("1")).thenReturn(CompletableDeferred(result))

        // When getting the list of comments
        val response = dataSource.getComments(listOf(1L))

        // Then the response is the expected one
        assertNotNull(response)
        assertEquals(Result.Success(repliesResponses), response)
    }

    @Test
    fun getComments_forMultipleComments() = runBlocking {
        // Given that the service responds with success for specific ids
        val result = Response.success(repliesResponses)
        Mockito.`when`(service.getComments("11,12")).thenReturn(CompletableDeferred(result))

        // When getting the list of comments for specific list of ids
        val response = dataSource.getComments(listOf(11L, 12L))

        // Then the response is the expected one
        assertNotNull(response)
        assertEquals(Result.Success(repliesResponses), response)
    }

    @Test
    fun getComments_whenRequestFailed() = runBlocking {
        // Given that the service responds with failure
        val result = Response.error<List<CommentResponse>>(400,
            errorResponseBody
        )
        Mockito.`when`(service.getComments("1")).thenReturn(CompletableDeferred(result))

        // When getting the list of comments
        val response = dataSource.getComments(listOf(1L))

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }

    @Test
    fun getComments_whenResponseEmpty() = runBlocking {
        // Given that the service responds with success but with an empty response
        val result = Response.success<List<CommentResponse>>(null)
        Mockito.`when`(service.getComments("1")).thenReturn(CompletableDeferred(result))

        // When getting the list of comments
        val response = dataSource.getComments(listOf(1L))

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }
}
