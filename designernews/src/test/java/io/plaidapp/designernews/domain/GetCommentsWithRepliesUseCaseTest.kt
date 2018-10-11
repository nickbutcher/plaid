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

package io.plaidapp.designernews.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.comments.CommentsRepository
import io.plaidapp.designernews.parentCommentResponse
import io.plaidapp.designernews.parentCommentWithReplies
import io.plaidapp.designernews.parentCommentWithRepliesWithoutReplies
import io.plaidapp.designernews.reply1
import io.plaidapp.designernews.replyResponse1
import io.plaidapp.designernews.replyResponse2
import io.plaidapp.designernews.replyWithReplies1
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Tests for [GetCommentsWithRepliesUseCase] where all the dependencies are mocked.
 */
class GetCommentsWithRepliesUseCaseTest {

    private val repository: CommentsRepository = mock()
    private val useCase = GetCommentsWithRepliesUseCase(repository)

    @Test
    fun getComments_noReplies_whenRequestSuccessful() = runBlocking {
        // Given that the repository responds with success
        val ids = listOf(reply1.id)
        val repositoryResult = Result.Success(listOf(replyResponse1))
        whenever(repository.getComments(ids)).thenReturn(repositoryResult)

        // When getting the replies
        val result = useCase(ids)

        // Then the correct list of comments was requested
        verify(repository).getComments(ids)
        // Then the correct list is received
        assertEquals(Result.Success(listOf(replyWithReplies1)), result)
    }

    @Test
    fun getComments_noReplies_whenRequestFailed() = runBlocking {
        // Given that the repository responds with error
        val ids = listOf(11L)
        val repositoryResult = Result.Error(IOException("Unable to get comments"))
        whenever(repository.getComments(ids)).thenReturn(repositoryResult)

        // When getting the comments
        val result = useCase(ids)

        // Then the result is not successful
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }

    @Test
    fun getComments_multipleReplies_whenRequestSuccessful() = runBlocking {
        // Given that:
        // When requesting replies for ids 1 from repository we get the parent comment but
        // without replies embedded (since that's what the next call is doing)
        val resultParent = Result.Success(listOf(parentCommentResponse))
        val parentIds = listOf(1L)
        whenever(repository.getComments(parentIds)).thenReturn(resultParent)
        // When requesting replies for ids 11 and 12 from repository we get the children
        val childrenIds = listOf(11L, 12L)
        val resultChildren = Result.Success(
            listOf(
                replyResponse1,
                replyResponse2
            )
        )
        whenever(repository.getComments(childrenIds)).thenReturn(resultChildren)

        // When getting the comments from the useCase
        val result = useCase(listOf(1L))

        // Then  requests were triggered
        verify(repository).getComments(parentIds)
        verify(repository).getComments(childrenIds)
        // Then the correct result is received
        assertEquals(Result.Success(arrayListOf(parentCommentWithReplies)), result)
    }

    @Test
    fun getComments_multipleReplies_whenRepliesRequestFailed() = runBlocking {
        // Given that
        // When requesting replies for ids 1 from repository we get the parent comment
        val resultParent = Result.Success(listOf(parentCommentResponse))
        val parentIds = listOf(1L)
        whenever(repository.getComments(parentIds)).thenReturn(resultParent)
        // When requesting replies for ids 11 and 12 from repository we get an error
        val resultChildrenError = Result.Error(IOException("Unable to get comments"))
        val childrenIds = listOf(11L, 12L)
        whenever(repository.getComments(childrenIds)).thenReturn(resultChildrenError)

        // When getting the comments from the useCase
        val result = useCase(listOf(1L))

        // Then  API requests were triggered
        verify(repository).getComments(parentIds)
        verify(repository).getComments(childrenIds)
        // Then the correct result is received
        assertEquals(Result.Success(arrayListOf(parentCommentWithRepliesWithoutReplies)), result)
    }
}
