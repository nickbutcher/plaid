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

package io.plaidapp.core.designernews.data.users

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.errorResponseBody
import io.plaidapp.core.designernews.users
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests for [UserRemoteDataSource] with mocked dependencies.
 */
class UserRemoteDataSourceTest {

    private val service: DesignerNewsService = mock()
    private val dataSource = UserRemoteDataSource(service)

    @Test
    fun getUsers_withSuccess() = runBlocking {
        // Given that the service responds with success
        withUsersSuccess("111,222", users)

        // When requesting the users
        val result = dataSource.getUsers(listOf(111L, 222L))

        // Then there's one request to the service
        verify(service).getUsers("111,222")
        // Then the correct set of users is returned
        assertEquals(Result.Success(users), result)
    }

    @Test
    fun getUsers_withError() = runBlocking {
        // Given that the service responds with error
        withUsersError("111,222")

        // When requesting the users
        val result = dataSource.getUsers(listOf(111L, 222L))

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    private fun withUsersSuccess(ids: String, users: List<User>) {
        val result = Response.success(users)
        whenever(service.getUsers(ids)).thenReturn(CompletableDeferred(result))
    }

    private fun withUsersError(ids: String) {
        val result = Response.error<List<User>>(
            400,
            errorResponseBody
        )
        whenever(service.getUsers(ids)).thenReturn(CompletableDeferred(result))
    }
}
