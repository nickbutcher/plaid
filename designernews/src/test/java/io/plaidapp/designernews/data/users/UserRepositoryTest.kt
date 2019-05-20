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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.users.model.User
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Test for [UserRepository] that mocks all the dependencies.
 */
class UserRepositoryTest {
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

    private val dataSource: UserRemoteDataSource = mock()
    private val repository = UserRepository(dataSource)

    @Test
    fun getUsers_withNoCachedUsers_withSuccess() = runBlocking {
        // Given that the dataSource responds with success
        val ids = listOf(111L, 222L)
        withUsersSuccess(ids, users)

        // When requesting the users
        val result = repository.getUsers(setOf(111L, 222L))

        // Then there's one request to the dataSource
        verify(dataSource).getUsers(ids)
        // Then the correct set of users is returned
        assertEquals(Result.Success(users.toSet()), result)
    }

    @Test
    fun getUsers_withNoCachedUsers_withError() = runBlocking {
        // Given that the dataSource responds with error
        val ids = listOf(111L, 222L)
        withUsersError(ids)

        // When requesting the users
        val result = repository.getUsers(ids.toSet())

        // Then error is returned
        assertTrue(result is Result.Error)
    }

    @Test
    fun getUsers_withCachedUsers_withSuccess() = runBlocking {
        // Given a user that was already requested and cached
        withUsersSuccess(listOf(111L), listOf(user1))
        repository.getUsers(setOf(111L))
        // Given another user that can be requested
        withUsersSuccess(listOf(222L), listOf(user2))

        // When requesting a list of users
        val result = repository.getUsers(setOf(111L, 222L))
        // Then there's one request to the dataSource
        verify(dataSource).getUsers(listOf(222L))
        // Then the correct set of users is returned
        assertEquals(Result.Success(users.toSet()), result)
    }

    @Test
    fun getUsers_withCachedUsers_withError() = runBlocking {
        // Given a user that was already requested and cached
        withUsersSuccess(listOf(111L), listOf(user1))
        repository.getUsers(setOf(111L))
        // Given that the dataSource responds with error for another users
        withUsersError(listOf(222L))

        // When requesting the users
        val result = repository.getUsers(setOf(111L, 222L))

        // We get the cached user
        assertEquals(Result.Success(setOf(user1)), result)
    }

    private fun withUsersSuccess(ids: List<Long>, users: List<User>) = runBlocking {
        val result = Result.Success(users)
        whenever(dataSource.getUsers(ids)).thenReturn(result)
    }

    private fun withUsersError(ids: List<Long>) = runBlocking {
        val result = Result.Error(IOException("Users error"))
        whenever(dataSource.getUsers(ids)).thenReturn(result)
    }
}
