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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.data.votes.VotesRepository
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.IOException

/**
 * Tests for [UpvoteStoryUseCase] mocking dependencies.
 */
class UpvoteStoryUseCaseTest {

    private val storyId = 24L
    private val userId = 63L
    private val user = User(
        id = userId,
        firstName = "Plaicent",
        lastName = "van Plaid",
        displayName = "Plaicent van Plaid",
        portraitUrl = "www"
    )

    private val loginRepository: LoginRepository = mock()
    private val votesRepository: VotesRepository = mock()
    private val upvoteStoryUseCase = UpvoteStoryUseCase(loginRepository, votesRepository)

    @Test(expected = IllegalStateException::class)
    fun upvoteStory_throws_whenUserNull() {
        // Given that the user is null
        whenever(loginRepository.user).thenReturn(null)

        // When upvoting a story
        // Then an exception is thrown
        runBlocking { upvoteStoryUseCase.upvoteStory(1L) }
    }

    @Test
    fun upvoteStory_whenStoryUpvotedSuccessfully() = runBlocking {
        // Given that the login repository returns a user
        whenever(loginRepository.user).thenReturn(user)
        // And the repository upvoted successfully the story
        whenever(votesRepository.upvoteStory(storyId, userId))
            .thenReturn(Result.Success(Unit))

        // When upvoting a story
        val result = upvoteStoryUseCase.upvoteStory(storyId)

        // Then the use case returns success
        Assert.assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteStory_whenStoryUpvotedFailed() = runBlocking {
        // Given that the login repository returns a user
        whenever(loginRepository.user).thenReturn(user)
        // And the repository story upvote failed
        whenever(votesRepository.upvoteStory(storyId, userId))
            .thenReturn(Result.Error(IOException("error")))

        // When upvoting a story
        val result = upvoteStoryUseCase.upvoteStory(storyId)

        // Then the use case returns with error
        Assert.assertTrue(result is Result.Error)
    }
}
