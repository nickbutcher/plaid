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

package io.plaidapp.core.designernews.ui.login

import android.arch.core.executor.testing.InstantTaskExecutorRule
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.test.shared.LiveDataTestUtil
import io.plaidapp.designernews.ui.login.LoginUiModel
import io.plaidapp.designernews.ui.login.LoginViewModel
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.io.IOException

/**
 * Class that tests [LoginViewModel] by mocking all the dependencies.
 */
class LoginViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val username = "Plaid"
    private val pass = "design"

    private val loginRepo = mock(LoginRepository::class.java)
    private val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

    @Test
    fun successEmitted_whenUserLoggedInSuccessfully() = runBlocking {
        // Given that the repository returns a user
        val user = User(
            id = 3,
            firstName = "Plaida",
            lastName = "Plaidich",
            displayName = "Plaida Plaidich",
            portraitUrl = "www"
        )
        val uiModel = LoginUiModel("plaida plaidich", "www")
        Mockito.`when`(loginRepo.login(username, pass)).thenReturn(Result.Success(user))

        // When logging in
        viewModel.login(username, pass)

        // Then the correct UI model is created
        val event = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(Result.Success(uiModel), event)
    }

    @Test
    fun errorEmitted_whenUserLogInFailed() = runBlocking {
        // Given that the repository returns with error
        Mockito.`when`(loginRepo.login(username, pass))
            .thenReturn(Result.Error(IOException("Login error")))

        // When logging in
        viewModel.login(username, pass)

        // Then the correct UI model is created
        val event = LiveDataTestUtil.getValue(viewModel.uiState)
        assertTrue(event is Result.Error)
    }

    @Test
    fun loadingIgnored_whenLoadingEmitted() = runBlocking {
        // Given that the repository returns loading
        Mockito.`when`(loginRepo.login(username, pass)).thenReturn(Result.Loading)

        // When logging in
        viewModel.login(username, pass)

        // Then only the initial loading event was triggered
        val event = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(Result.Loading, event)
    }
}
