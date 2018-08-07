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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.repliesResponses
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * Tests for [CommentsRepository] mocking all dependencies
 */
class CommentsRepositoryTest {
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
}
