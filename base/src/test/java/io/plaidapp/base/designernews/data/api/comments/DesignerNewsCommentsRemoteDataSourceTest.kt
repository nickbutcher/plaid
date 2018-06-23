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

package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response

/**
 * Tests for [DesignerNewsCommentsRemoteDataSource] that mock the Designer News API
 */
class DesignerNewsCommentsRemoteDataSourceTest {

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val dataSource = DesignerNewsCommentsRemoteDataSource(service)

    @Test
    fun getComments_whenRequestSuccessful() {
        // Given that the service responds with success
        val result = Response.success(replies)
        Mockito.`when`(service.getComments("1")).thenReturn(CompletableDeferred(result))

        runBlocking {
            // When getting the list of comments
            val response = dataSource.getComments(listOf(1L)).await()

            // Then the response is the expected one
            assertNotNull(response)
            assertEquals(replies, response)
        }
    }

    @Test
    fun getComments_forMultipleComments() {
        // Given that the service responds with success for specific ids
        val result = Response.success(replies)
        Mockito.`when`(service.getComments("11,12")).thenReturn(CompletableDeferred(result))

        runBlocking {
            // When getting the list of comments for specific list of ids
            val response = dataSource.getComments(listOf(11L, 12L)).await()

            // Then the response is the expected one
            assertNotNull(response)
            assertEquals(replies, response)
        }
    }

    @Test
    fun getComments_whenRequestFailed() {
        // Given that the service responds with failure
        val result = Response.error<List<Comment>>(400, errorResponseBody)
        Mockito.`when`(service.getComments("1")).thenReturn(CompletableDeferred(result))

        runBlocking {
            // When getting the list of comments
            val response = dataSource.getComments(listOf(1L)).await()

            // Then the response is null
            assertNull(response)
        }
    }

    @Test
    fun getComments_whenResponseEmpty() {
        // Given that the service responds with success but with an empty response
        val result = Response.success<List<Comment>>(null)
        Mockito.`when`(service.getComments("1")).thenReturn(CompletableDeferred(result))

        runBlocking {
            // When getting the list of comments
            val response = dataSource.getComments(listOf(1L)).await()

            // Then the response is null
            assertNull(response)
        }
    }
}
