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
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginLocalDataSource
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRemoteDataSource
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRepository
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import retrofit2.mock.Calls
import java.io.IOException

/**
 * Tests for [DesignerNewsLoginRepository] using shared preferences from instrumentation context
 * for building the local and remote data sources, and mocked API service.
 */
class DesignerNewsLoginRepositoryTest {

    private val user = User(id = 3, displayName = "Plaida Plaidich", portraitUrl = "www")
    private val accessToken = AccessToken("token")

    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)
    private var localDataSource = DesignerNewsLoginLocalDataSource(sharedPreferences)

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val remoteDataSource = DesignerNewsLoginRemoteDataSource(
            DesignerNewsAuthTokenLocalDataSource(sharedPreferences), service)

    private val repository = DesignerNewsLoginRepository(localDataSource, remoteDataSource)

    @After
    fun tearDown() {
        // cleanup the shared preferences after every test
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun isNotLoggedIn_byDefault() {
        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun isLoggedIn_afterSuccessfulLogin() {
        // Given that the login will be successful
        withLoginSuccessful()
        var actualUser: User? = null

        // When logging in
        repository.login("user", "pass", { it -> actualUser = it }, { Assert.fail() })

        // Then the success callback was called
        assertEquals(user, actualUser)
        // The user is logged in
        assertTrue(repository.isLoggedIn)
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
    }

    @Test
    fun logout_afterLogin() {
        // Given a logged in user
        withLoginSuccessful()
        repository.login("user", "pass", { it -> assertEquals(user, it) }, { Assert.fail() })

        // When logging out
        repository.logout()

        // Then the user is logged out
        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun isNotLoggedIn_afterFailedLogin() {
        // Given that the login will fail
        withLoginFailed()
        var errorCalled = false

        // When logging in
        repository.login("user", "pass", { Assert.fail() }, { errorCalled = true })

        // Then the error callback was called
        assertTrue(errorCalled)
        // The user is not logged in
        assertFalse(repository.isLoggedIn)
    }

    private fun withLoginSuccessful() {
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(Calls.response(accessToken))
        Mockito.`when`(service.getAuthedUser()).thenReturn(Calls.response(arrayListOf(user)))
    }

    private fun withLoginFailed() {
        Mockito.`when`(service.login(Mockito.anyMap()))
                .thenReturn(Calls.failure(IOException("test")))
    }
}
