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

package io.plaidapp.designernews.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.designernews.data.comments.CommentsRepository
import io.plaidapp.designernews.loggedInUser
import io.plaidapp.designernews.replyResponse1
import io.plaidapp.designernews.user1
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [PostReplyUseCase] mocking all dependencies.
 */
class PostReplyUseCaseTest {
    private val body = "Plaid is plaidy"

    private val repository: CommentsRepository = mock()
    private val loginRepository: LoginRepository = mock()
    private val postReply = PostReplyUseCase(repository, loginRepository)

    @Test(expected = IllegalStateException::class)
    fun postReply_userNull() = runBlocking {
        // Given that a user is not logged in
        whenever(loginRepository.user).thenReturn(null)

        // When logging in
        postReply("text", 123L)

        // Then an exception is thrown
        Unit
    }

    @Test
    fun postReply_errorReturned() = runBlocking {
        // Given a logged in user
        whenever(loginRepository.user).thenReturn(loggedInUser)
        // Given that the comment is posted with error
        whenever(repository.postReply(body, 123L, 111L))
            .thenReturn(Result.Error(IOException("Error")))

        // When posting a comment to a story
        val result = postReply(body, 123L)

        // Then the result is not successful
        assertTrue(result is Result.Error)
    }

    @Test
    fun postReply_success() = runBlocking {
        // Given a logged in user
        whenever(loginRepository.user).thenReturn(loggedInUser)
        // Given that the comment is posted successfully
        whenever(repository.postReply(replyResponse1.body, 123L, 111L))
            .thenReturn(Result.Success(replyResponse1))

        // When posting a comment to a story
        val result = postReply(replyResponse1.body, 123L)

        val expectedComment = Comment(
            id = replyResponse1.id,
            parentCommentId = replyResponse1.links.parentComment,
            body = replyResponse1.body,
            createdAt = replyResponse1.created_at,
            depth = replyResponse1.depth,
            upvotesCount = replyResponse1.links.commentUpvotes.size,
            userId = user1.id,
            userDisplayName = user1.displayName,
            userPortraitUrl = user1.portraitUrl,
            upvoted = false
        )
        // Then the result is the expected one
        assertEquals(Result.Success(expectedComment), result)
    }
}
