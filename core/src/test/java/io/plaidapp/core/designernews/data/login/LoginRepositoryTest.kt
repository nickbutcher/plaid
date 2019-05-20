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

package io.plaidapp.core.designernews.data.login

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Tests for [LoginRepository] using shared preferences from instrumentation context
 * for building the local and remote data sources, and mocked API service.
 */
class LoginRepositoryTest {

    private val username = "user"
    private val pass = "pass"
    private val user = LoggedInUser(
        id = 3,
        firstName = "Plaida",
        lastName = "Plaidich",
        displayName = "Plaida Plaidich",
        portraitUrl = "www",
        upvotes = listOf(1L, 2L, 444L)
    )

    private var localDataSource: LoginLocalDataSource = mock()
    private val remoteDataSource: LoginRemoteDataSource = mock()
    private val repository = LoginRepository(localDataSource, remoteDataSource)

    @Test
    fun isNotLoggedIn_byDefault() {
        // When no user was logged in
        // Then the logged in repository flag is false
        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun isLoggedIn_afterSuccessfulLogin() = runBlocking {
        // Given that the login will be successful
        withLoginSuccessful(username, pass)

        // When logging in
        val result = repository.login(username, pass)

        // Then the success result is returned
        assertEquals(Result.Success(user), result)
        // The user is logged in
        assertTrue(repository.isLoggedIn)
        // The user cached is the expected user
        assertEquals(repository.user, user)
    }

    @Test
    fun userNull_byDefault() {
        assertNull(repository.user)
    }

    @Test
    fun logout() {
        // When logging out
        repository.logout()

        // Then the user is not logged in
        assertFalse(repository.isLoggedIn)
        // The cached user is null
        assertNull(repository.user)
    }

    @Test
    fun logout_afterLogin() = runBlocking {
        // Given a logged in user
        withLoginSuccessful(username, pass)
        repository.login(username, pass)

        // When logging out
        repository.logout()

        // Then the user is logged out
        assertFalse(repository.isLoggedIn)
        // The cached user is null
        assertNull(repository.user)
    }

    @Test
    fun isNotLoggedIn_afterFailedLogin() = runBlocking {
        // Given that the login will fail
        withLoginFailed(username, pass)

        // When logging in
        val result = repository.login(username, pass)

        // Then the error result is returned
        assertTrue(result is Result.Error)
        // The user is not logged in
        assertFalse(repository.isLoggedIn)
        // The cached user is null
        assertNull(repository.user)
    }

    private fun withLoginSuccessful(username: String, pass: String) = runBlocking {
        val result = Result.Success(user)
        whenever(remoteDataSource.login(username, pass)).thenReturn(result)
    }

    private fun withLoginFailed(username: String, pass: String) = runBlocking {
        whenever(remoteDataSource.login(username, pass))
            .thenReturn(Result.Error(IOException("error")))
    }
}
