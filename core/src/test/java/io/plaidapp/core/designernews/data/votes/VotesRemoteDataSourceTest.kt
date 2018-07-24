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
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.api.any
import io.plaidapp.core.designernews.errorResponseBody
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Test for [VotesRepository] mocking all dependencies.
 */
class VotesRemoteDataSourceTest {

    private val userId = 3L
    private val storyId = 1345L

    private val service: DesignerNewsService = mock()
    private val dataSource = VotesRemoteDataSource(service)

    @Test
    fun upvoteStory_whenRequestSuccessful() = runBlocking {
        // Given that the service responds with success
        val response = Response.success(Unit)
        whenever(service.upvoteStoryV2(any())).thenReturn(CompletableDeferred(response))

        // When upvoting a story
        val result = dataSource.upvoteStory(storyId, userId)

        // Then the result is successful
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteStory_whenRequestFailed() = runBlocking {
        // Given that the service responds with error
        val response = Response.error<Unit>(404, errorResponseBody)
        whenever(service.upvoteStoryV2(any())).thenReturn(CompletableDeferred(response))

        // When upvoting a story
        val result = dataSource.upvoteStory(storyId, userId)

        // Then the result is error
        assertTrue(result is Result.Error)
    }

    @Test
    fun upvoteComment_whenRequestSuccessful() = runBlocking {
        // Given that the service responds with success
        val response = Response.success(Unit)
        whenever(service.upvoteComment(any())).thenReturn(CompletableDeferred(response))

        // When upvoting a story
        val result = dataSource.upvoteComment(storyId, userId)

        // Then the result is successful
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteComment_whenRequestFailed() = runBlocking {
        // Given that the service responds with error
        val response = Response.error<Unit>(404, errorResponseBody)
        whenever(service.upvoteComment(any())).thenReturn(CompletableDeferred(response))

        // When upvoting a story
        val result = dataSource.upvoteComment(storyId, userId)

        // Then the result is error
        assertTrue(result is Result.Error)
    }
}
