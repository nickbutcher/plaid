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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.designernews.repliesResponses
import io.plaidapp.designernews.replyResponse1
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [CommentsRepository] mocking all dependencies
 */
class CommentsRepositoryTest {
    private val body = "Plaid 2.0 is awesome"

    private val dataSource: CommentsRemoteDataSource = mock()
    private val repository = CommentsRepository(dataSource)

    @Test
    fun getComments_withSuccess() = runBlocking {
        // Given a list of comment responses that are return for a specific list of ids
        val ids = listOf(1L)
        val result = Result.Success(repliesResponses)
        whenever(dataSource.getComments(ids)).thenReturn(result)

        // When requesting the comments
        val data = repository.getComments(ids)

        // The correct response is returned
        assertEquals(result, data)
    }

    @Test
    fun getComments_withError() = runBlocking {
        // Given a list of comment responses that are return for a specific list of ids
        val ids = listOf(1L)
        val result = Result.Error(IOException("error"))
        whenever(dataSource.getComments(ids)).thenReturn(result)

        // When requesting the comments
        val data = repository.getComments(ids)

        // The correct response is returned
        assertEquals(result, data)
    }

    @Test
    fun postStoryComment_withSuccess() = runBlocking {
        // Given that a result is return when posting a story comment
        val result = Result.Success(replyResponse1)
        whenever(
            dataSource.comment(
                commentBody = body,
                parentCommentId = null,
                storyId = 11L,
                userId = 111L
            )
        ).thenReturn(result)

        // When posting a story comment
        val data = repository.postStoryComment(body, 11L, 111L)

        // The correct response is returned
        assertEquals(result, data)
    }

    @Test
    fun postStoryComment_withError() = runBlocking {
        // Given that an error result is return when posting a story comment
        val result = Result.Error(IOException("error"))
        whenever(
            dataSource.comment(
                commentBody = body,
                parentCommentId = null,
                storyId = 11L,
                userId = 111L
            )
        ).thenReturn(result)

        // When posting a story comment
        val data = repository.postStoryComment(body, 11L, 111L)

        // The correct response is returned
        assertEquals(result, data)
    }

    @Test
    fun postReply_withSuccess() = runBlocking {
        // Given that a result is return when posting a story comment
        val result = Result.Success(replyResponse1)
        whenever(
            dataSource.comment(
                commentBody = body,
                parentCommentId = 11L,
                storyId = null,
                userId = 111L
            )
        ).thenReturn(result)

        // When posting reply
        val data = repository.postReply(body, 11L, 111L)

        // The correct response is returned
        assertEquals(result, data)
    }

    @Test
    fun postReply_withError() = runBlocking {
        // Given that an error result is return when posting a reply to a comment
        val result = Result.Error(IOException("error"))
        whenever(
            dataSource.comment(
                commentBody = body,
                parentCommentId = 11L,
                storyId = null,
                userId = 111L
            )
        ).thenReturn(result)

        // When posting reply
        val data = repository.postReply(body, 11L, 111L)

        // The correct response is returned
        assertEquals(result, data)
    }
}
