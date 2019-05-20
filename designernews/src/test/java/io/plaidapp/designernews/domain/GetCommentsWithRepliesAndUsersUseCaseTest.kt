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
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.domain.model.CommentWithReplies
import io.plaidapp.designernews.data.users.UserRepository
import io.plaidapp.designernews.flattendCommentsWithReplies
import io.plaidapp.designernews.flattenedCommentsWithoutReplies
import io.plaidapp.designernews.parentCommentWithReplies
import io.plaidapp.designernews.parentCommentWithRepliesWithoutReplies
import io.plaidapp.designernews.reply1
import io.plaidapp.designernews.reply1NoUser
import io.plaidapp.designernews.replyWithReplies1
import io.plaidapp.designernews.user1
import io.plaidapp.designernews.user2
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Tests for [GetCommentsWithRepliesAndUsersUseCase] where all the dependencies are mocked.
 */
class GetCommentsWithRepliesAndUsersUseCaseTest {
    private val getCommentsWithReplies: GetCommentsWithRepliesUseCase = mock()
    private val userRepository: UserRepository = mock()
    private val repository = GetCommentsWithRepliesAndUsersUseCase(
        getCommentsWithReplies,
        userRepository
    )

    @Test
    fun getComments_noReplies_whenCommentsAnUserRequestsSuccessful() = runBlocking {
        // Given that the comments request responds with success
        val ids = listOf(11L)
        withComment(replyWithReplies1, ids)
        // Given that the user request responds with success
        withUsers(setOf(user1), setOf(111L))

        // When getting the replies
        val result = repository(ids)

        // Then the correct list is received
        assertEquals(Result.Success(listOf(reply1)), result)
    }

    @Test
    fun getComments_noReplies_whenCommentsRequestFailed() = runBlocking {
        // Given that the getCommentsWithReplies responds with failure
        val resultError = Result.Error(IOException("Comment error"))
        val ids = listOf(11L)
        whenever(getCommentsWithReplies.invoke(ids)).thenReturn(resultError)

        // When getting the comments
        val result = repository(ids)

        // Then the result is not successful
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }

    @Test
    fun getComments_multipleReplies_whenCommentsAndUsersRequestsSuccessful() = runBlocking {
        // Given that:
        // When requesting replies for ids 1 from getCommentsWithReplies we get the parent comment but
        // without replies embedded (since that's what the next call is doing)
        val parentIds = listOf(1L)
        withComment(parentCommentWithReplies, parentIds)
        withUsers(setOf(user1, user2), setOf(111L, 222L))

        // When getting the comments from the repository
        val result = repository(listOf(1L))

        // Then comments were requested for correct ids
        verify(getCommentsWithReplies).invoke(parentIds)
        // Then the correct result is received
        assertEquals(Result.Success(flattendCommentsWithReplies), result)
    }

    @Test
    fun getComments_multipleReplies_whenRepliesRequestFailed() = runBlocking {
        // Given that when requesting replies for ids 1 from getCommentsWithReplies we get the parent comment
        val parentIds = listOf(1L)
        withComment(parentCommentWithRepliesWithoutReplies, parentIds)
        // Given that the user request responds with success
        withUsers(setOf(user2), setOf(222))

        // When getting the comments from the repository
        val result = repository(listOf(1L))

        // Then comments were requested for correct ids
        verify(getCommentsWithReplies).invoke(parentIds)
        // Then the correct result is received
        assertEquals(Result.Success(flattenedCommentsWithoutReplies), result)
    }

    @Test
    fun getComments_whenUserRequestFailed() = runBlocking {
        // Given that:
        // When requesting replies for ids 1 from getCommentsWithReplies we get the parent comment but
        // without replies embedded (since that's what the next call is doing)
        val ids = listOf(11L)
        withComment(replyWithReplies1, ids)
        // Given that the user request responds with failure
        val userError = Result.Error(IOException("User error"))
        whenever(userRepository.getUsers(setOf(11L))).thenReturn(userError)

        // When getting the comments from the repository
        val result = repository(listOf(11L))

        // Then comments were requested for correct ids
        verify(getCommentsWithReplies).invoke(ids)
        // Then the correct result is received
        assertEquals(Result.Success(arrayListOf(reply1NoUser)), result)
    }

    // Given that the users request responds with success
    private fun withUsers(users: Set<User>, ids: Set<Long>) = runBlocking {
        val userResult = Result.Success(users)
        whenever(userRepository.getUsers(ids)).thenReturn(userResult)
    }

    private fun withComment(comment: CommentWithReplies, ids: List<Long>) = runBlocking {
        val resultParent = Result.Success(listOf(comment))
        whenever(getCommentsWithReplies(ids)).thenReturn(resultParent)
    }
}
