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
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import io.plaidapp.designernews.data.votes.VotesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Tests for [UpvoteCommentUseCase] mocking dependencies.
 */
class UpvoteCommentUseCaseTest {

    private val commentId = 24L
    private val userId = 63L
    private val user = LoggedInUser(
        id = userId,
        firstName = "Plaicent",
        lastName = "van Plaid",
        displayName = "Plaicent van Plaid",
        portraitUrl = "www",
        upvotes = listOf(1L, 2L, 3L)
    )

    private val loginRepository: LoginRepository = mock()
    private val votesRepository: VotesRepository = mock()
    private val upvoteCommentUseCase = UpvoteCommentUseCase(loginRepository, votesRepository)

    @Test(expected = IllegalStateException::class)
    fun upvoteComment_throws_whenUserNull() {
        // Given that the user is null
        whenever(loginRepository.user).thenReturn(null)

        // When upvoting a comment
        // Then an exception is thrown
        runBlocking { upvoteCommentUseCase(1L) }
    }

    @Test
    fun upvoteComment_whenCommentUpvotedSuccessfully() = runBlocking {
        // Given that the login repository returns a user
        whenever(loginRepository.user).thenReturn(user)
        // And the repository upvoted successfully
        whenever(votesRepository.upvoteComment(commentId, userId))
            .thenReturn(Result.Success(Unit))

        // When upvoting a comment
        val result = upvoteCommentUseCase(commentId)

        // Then the use case returns success
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteComment_whenCommentUpvotedFailed() = runBlocking {
        // Given that the login repository returns a user
        whenever(loginRepository.user).thenReturn(user)
        // And the repository upvote failed
        whenever(votesRepository.upvoteComment(commentId, userId))
            .thenReturn(Result.Error(IOException("error")))

        // When upvoting a comment
        val result = upvoteCommentUseCase(commentId)

        // Then the use case returns with error
        assertTrue(result is Result.Error)
    }
}
