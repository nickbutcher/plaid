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

package io.plaidapp.designernews.data.comments

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.comments.model.CommentResponse
import io.plaidapp.designernews.data.comments.model.NewCommentRequest
import io.plaidapp.designernews.data.comments.model.PostCommentResponse
import io.plaidapp.designernews.errorResponseBody
import io.plaidapp.designernews.repliesResponses
import io.plaidapp.designernews.replyResponse1
import java.net.UnknownHostException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests for [CommentsRemoteDataSource] that mock the Designer News API
 */
class CommentsRemoteDataSourceTest {

    private val body = "Plaid is awesome"

    private val service: DesignerNewsService = mock()
    private val dataSource = CommentsRemoteDataSource(service)

    @Test
    fun getComments_whenRequestSuccessful() = runBlocking {
        // Given that the service responds with success
        val result = Response.success(repliesResponses)
        whenever(service.getComments("1")).thenReturn(result)

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
        whenever(service.getComments("11,12")).thenReturn(result)

        // When getting the list of comments for specific list of ids
        val response = dataSource.getComments(listOf(11L, 12L))

        // Then the response is the expected one
        assertNotNull(response)
        assertEquals(Result.Success(repliesResponses), response)
    }

    @Test
    fun getComments_whenRequestFailed() = runBlocking {
        // Given that the service responds with failure
        val result = Response.error<List<CommentResponse>>(
            400,
            errorResponseBody
        )
        whenever(service.getComments("1")).thenReturn(result)

        // When getting the list of comments
        val response = dataSource.getComments(listOf(1L))

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }

    @Test
    fun getComments_whenResponseEmpty() = runBlocking {
        // Given that the service responds with success but with an empty response
        val result = Response.success<List<CommentResponse>>(null)
        whenever(service.getComments("1")).thenReturn(result)

        // When getting the list of comments
        val response = dataSource.getComments(listOf(1L))

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }

    @Test
    fun getComments_whenException() = runBlocking {
        // Given that the service throws an exception
        doAnswer { throw UnknownHostException() }
            .whenever(service).getComments("1")

        // When getting the list of comments
        val response = dataSource.getComments(listOf(1L))

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }

    @Test(expected = IllegalStateException::class)
    fun comment_whenParentCommentIdAndStoryIdNull() = runBlocking {
        // When posting a comment with both the parent comment id and the story id are null
        dataSource.comment("text", null, null, 11L)
        // Then an exception is thrown
        Unit
    }

    @Test
    fun comment_whenException() = runBlocking {
        // Given that the service throws an exception
        val request = NewCommentRequest(body, "11", null, "111")
        doAnswer { throw UnknownHostException() }
            .whenever(service).comment(request)

        // When adding a comment
        val response = dataSource.comment(body, 11L, null, 111L)

        // Then the response is not successful
        assertTrue(response is Result.Error)
    }

    @Test
    fun comment_withNoComments() = runBlocking {
        // Given a response returned for a request
        val response = Response.success(
            PostCommentResponse(
                emptyList()
            )
        )
        val request = NewCommentRequest(body, "11", null, "111")
        whenever(service.comment(request)).thenReturn(response)

        // When adding a comment
        val result = dataSource.comment(body, 11L, null, 111L)

        // Then the result is not successful
        assertTrue(result is Result.Error)
    }

    @Test
    fun comment_withComments() = runBlocking {
        // Given a response returned for a request
        val response = Response.success(
            PostCommentResponse(listOf(replyResponse1))
        )
        val request = NewCommentRequest(body, "11", null, "111")
        whenever(service.comment(request)).thenReturn(response)

        // When adding a comment
        val result = dataSource.comment(body, 11L, null, 111L)

        // Then the result is successful
        assertEquals(result, Result.Success(replyResponse1))
    }
}
