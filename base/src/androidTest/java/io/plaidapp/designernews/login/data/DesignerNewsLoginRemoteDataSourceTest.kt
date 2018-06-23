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

package io.plaidapp.designernews.login.data

import android.content.Context
import android.support.test.InstrumentationRegistry
import io.plaidapp.base.designernews.data.api.DesignerNewsAuthTokenLocalDataSource
import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.AccessToken
import io.plaidapp.base.designernews.data.api.model.User
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRemoteDataSource
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import retrofit2.mock.Calls
import java.io.IOException

/**
 * Tests for [DesignerNewsLoginRemoteDataSource] using shared preferences from instrumentation
 * context and mocked API service.
 */
class DesignerNewsLoginRemoteDataSourceTest {

    private val user = User(id = 3, displayName = "Plaidy Plaidinski", portraitUrl = "www")
    private val accessToken = AccessToken("token")
    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val authTokenDataSource = DesignerNewsAuthTokenLocalDataSource(sharedPreferences)
    private val dataSource = DesignerNewsLoginRemoteDataSource(authTokenDataSource, service)

    @After
    fun tearDown() {
        // cleanup the shared preferences after every test
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun logout_clearsToken() {
        // When logging out
        dataSource.logout()

        // Then the auth token is null
        assertNull(authTokenDataSource.authToken)
    }

    @Test
    fun login_successful_when_AccessTokenAndGetUserSuccessful() {
        // Given that all API calls are successful
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(Calls.response(accessToken))
        Mockito.`when`(service.getAuthedUser()).thenReturn(Calls.response(arrayListOf(user)))
        var actualUser: User? = null

        // When logging in
        dataSource.login("test", "test", { it -> actualUser = it }, { Assert.fail() })

        // Then the user is received
        assertEquals(user, actualUser)
    }

    @Test
    fun login_failed_whenAccessTokenFailed() {
        // Given that the auth token retrieval fails
        val failureResponse = Calls.failure<AccessToken>(IOException("test"))
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(failureResponse)
        var errorCalled = false

        // When logging in
        dataSource.login(
                "test",
                "test",
                { Assert.fail("login network call failed so login should have failed") },
                { errorCalled = true })

        // Then the login fails
        assertTrue(errorCalled)
        Mockito.verify(service, Mockito.never()).getAuthedUser()
    }

    @Test
    fun login_failed_whenGetUserFailed() {
        // Given that the access token is retrieved successfully
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(Calls.response(accessToken))
        // And the get authed user failed
        val failureResponse = Calls.failure<List<User>>(IOException("test"))
        Mockito.`when`(service.getAuthedUser()).thenReturn(failureResponse)
        var errorCalled = false

        // When logging in
        dataSource.login(
                "test",
                "test",
                { Assert.fail("getAuthedUser failed so login should have failed") },
                { errorCalled = true })

        // Then error is triggered
        assertTrue(errorCalled)
    }
}
