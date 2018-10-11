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

package io.plaidapp.designernews.data.users

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.users.model.User
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException

/**
 * Tests for [UserRemoteDataSource] with mocked dependencies.
 */
class UserRemoteDataSourceTest {
    private val user1 = User(
        id = 111L,
        firstName = "Plaicent",
        lastName = "van Plaid",
        displayName = "Plaicent van Plaid",
        portraitUrl = "www"
    )
    private val user2 = User(
        id = 222L,
        firstName = "Plaude",
        lastName = "Pladon",
        displayName = "Plaude Pladon",
        portraitUrl = "www"
    )
    private val users = listOf(user1, user2)
    private val errorResponseBody = ResponseBody.create(MediaType.parse(""), "Error")

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

    @Test
    fun getUsers_withException() = runBlocking {
        // Given that the service throws an exception
        doAnswer { throw UnknownHostException() }
            .whenever(service).getUsers("111,222")

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
