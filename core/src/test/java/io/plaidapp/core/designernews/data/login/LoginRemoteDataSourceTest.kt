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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.login.model.AccessToken
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.errorResponseBody
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests for [LoginRemoteDataSource] using shared preferences from instrumentation
 * context and mocked API service.
 */
class LoginRemoteDataSourceTest {

    private val user = User(
        id = 3,
        firstName = "Plaidy",
        lastName = "Plaidinski",
        displayName = "Plaidy Plaidinski",
        portraitUrl = "www"
    )
    private val accessToken = AccessToken("token")

    private val service: DesignerNewsService = mock()
    private val authTokenDataSource: AuthTokenLocalDataSource = mock()
    private val dataSource = LoginRemoteDataSource(authTokenDataSource, service)

    @Test
    fun logout_clearsToken() {
        // When logging out
        dataSource.logout()

        // Then the auth token is null
        assertNull(authTokenDataSource.authToken)
    }

    @Test
    fun login_successful_when_AccessTokenAndGetUserSuccessful() = runBlocking {
        // Given that all API calls are successful
        val accessTokenResponse = Response.success(accessToken)
        whenever(service.login(any())).thenReturn(CompletableDeferred(accessTokenResponse))
        val authUserResponse = Response.success(listOf(user))
        whenever(service.getAuthedUser()).thenReturn(CompletableDeferred(authUserResponse))

        // When logging in
        val result = dataSource.login("test", "test")

        // Then the user is received
        assertEquals(Result.Success(user), result)
    }

    @Test
    fun login_failed_whenAccessTokenFailed() = runBlocking {
        // Given that the auth token retrieval fails
        val failureResponse = Response.error<AccessToken>(
            400,
            errorResponseBody
        )
        whenever(service.login(any())).thenReturn(CompletableDeferred(failureResponse))

        // When logging in
        val result = dataSource.login("test", "test")

        // Then get authed user is never called
        verify(service, never()).getAuthedUser()
        // Then the login fails
        assertTrue(result is Result.Error)
    }

    @Test
    fun login_failed_whenGetUserFailed() = runBlocking {
        // Given that the access token is retrieved successfully
        val accessTokenRespone = Response.success(accessToken)
        whenever(service.login(any())).thenReturn(CompletableDeferred(accessTokenRespone))
        // And the get authed user failed
        val failureResponse = Response.error<List<User>>(
            400,
            errorResponseBody
        )
        whenever(service.getAuthedUser()).thenReturn(CompletableDeferred(failureResponse))

        // When logging in
        val result = dataSource.login("test", "test")

        // Then error is triggered
        assertTrue(result is Result.Error)
    }
}
