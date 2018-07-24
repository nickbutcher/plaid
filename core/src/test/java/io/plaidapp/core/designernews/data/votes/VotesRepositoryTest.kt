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

package io.plaidapp.core.designernews.data.votes

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Test for [VotesRepository] mocking all dependencies.
 */
class VotesRepositoryTest {

    private val userId = 3L
    private val storyId = 1345L
    private val commentId = 999L

    private val dataSource: VotesRemoteDataSource = mock()
    private val votesRepository = VotesRepository(dataSource)

    @Test
    fun upvoteStory_whenRequestSuccessful() = runBlocking {
        // Given that the data source responds with success
        val response = Result.Success(Unit)
        whenever(dataSource.upvoteStory(storyId, userId)).thenReturn(response)

        // When upvoting a story
        val result = votesRepository.upvoteStory(storyId, userId)

        // Then the result is successful
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteStory_whenRequestFailed() = runBlocking {
        // Given that the data source responds with error
        val response = Result.Error(IOException("Error upvoting"))
        whenever(dataSource.upvoteStory(storyId, userId)).thenReturn(response)

        // When upvoting a story
        val result = votesRepository.upvoteStory(storyId, userId)

        // Then the result is error
        assertTrue(result is Result.Error)
    }

    @Test
    fun upvoteComment_whenRequestSuccessful() = runBlocking {
        // Given that the data source responds with success
        val response = Result.Success(Unit)
        whenever(dataSource.upvoteComment(commentId, userId)).thenReturn(response)

        // When upvoting a comment
        val result = votesRepository.upvoteComment(commentId, userId)

        // Then the result is successful
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteComment_whenRequestFailed() = runBlocking {
        // Given that the data source responds with error
        val response = Result.Error(IOException("Error upvoting"))
        whenever(dataSource.upvoteComment(commentId, userId)).thenReturn(response)

        // When upvoting a comment
        val result = votesRepository.upvoteComment(commentId, userId)

        // Then the result is error
        assertTrue(result is Result.Error)
    }
}
