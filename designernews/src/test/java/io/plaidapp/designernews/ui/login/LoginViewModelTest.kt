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

package io.plaidapp.designernews.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.R
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import io.plaidapp.core.util.event.Event
import io.plaidapp.test.shared.LiveDataTestUtil
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Class that tests [LoginViewModel] by mocking all the dependencies.
 */
class LoginViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val username = "Plaid"
    private val password = "design"
    private val initialUiModel = LoginUiModel(
        showProgress = false,
        showError = null,
        showSuccess = null,
        enableLoginButton = false
    )

    private val loginRepo: LoginRepository = mock()

    @Test
    fun login_whenUserLoggedInSuccessfully() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())
        // Given that the repository returns a user
        val user = LoggedInUser(
            id = 3,
            firstName = "Plaida",
            lastName = "Plaidich",
            displayName = "Plaida Plaidich",
            portraitUrl = "www",
            upvotes = listOf(123L, 234L)
        )

        whenever(loginRepo.login(username, password)).thenReturn(Result.Success(user))

        // When logging in
        viewModel.login(username, password)

        // Then the correct UI model is created
        val expected = LoginUiModel(
            showProgress = false,
            showError = null,
            showSuccess = Event(LoginResultUiModel("plaida plaidich", "www")),
            enableLoginButton = false
        )
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(expected, uiState)
    }

    @Test
    fun login_whenUserLogInFailed() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())
        // Given that the repository returns with error
        whenever(loginRepo.login(username, password))
            .thenReturn(Result.Error(IOException("Login error")))

        // When logging in
        viewModel.login(username, password)

        // Then the correct UI model is created
        val expectedUiModel = LoginUiModel(
            showProgress = false,
            showError = Event(R.string.login_failed),
            showSuccess = null,
            enableLoginButton = true
        )
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(expectedUiModel, uiState)
    }

    @Test
    fun init_disablesLogin() = runBlocking {
        // When the view model is created
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

        // Then the login is disabled
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(initialUiModel, uiState)
    }

    @Test
    fun loginDataChanged_withValidLogin() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

        // When login data changed with valid login data
        viewModel.loginDataChanged(username, password)

        // Then the correct UI model is created
        val expectedUiModel = LoginUiModel(
            showProgress = false,
            showError = null,
            showSuccess = null,
            enableLoginButton = true
        )
        // TODO leave only the last assert
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(expectedUiModel.showProgress, uiState!!.showProgress)
        assertEquals(expectedUiModel.showError, uiState.showError)
        assertEquals(expectedUiModel.showSuccess, uiState.showSuccess)
        assertEquals(expectedUiModel.enableLoginButton, uiState.enableLoginButton)
        assertEquals(expectedUiModel, uiState)
    }

    @Test
    fun loginDataChanged_withEmptyUsername() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

        // When login data changed with invalid login data
        viewModel.loginDataChanged("", password)

        // Then the correct UI model is created
        val expectedUiModel = LoginUiModel(
            showProgress = false,
            showError = null,
            showSuccess = null,
            enableLoginButton = false
        )
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(expectedUiModel, uiState)
    }

    @Test
    fun loginDataChanged_withEmptyPassword() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

        // When login data changed with invalid login data
        viewModel.loginDataChanged(username, "")

        // Then the correct UI model is created
        val expectedUiModel = LoginUiModel(
            showProgress = false,
            showError = null,
            showSuccess = null,
            enableLoginButton = false
        )
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(expectedUiModel, uiState)
    }

    @Test
    fun login_withEmptyUsername() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

        // When logging in with invalid login data
        viewModel.login("", password)

        // Then login is not triggered
        verify(loginRepo, never()).login(username, "")
        // Then the UI state is the initial state
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(initialUiModel, uiState)
    }

    @Test
    fun login_withEmptyPassword() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

        // When logging in with invalid login data
        viewModel.loginDataChanged(username, "")

        // Then login is not triggered
        verify(loginRepo, never()).login(username, "")
        // Then the UI state is the initial state
        val uiState = LiveDataTestUtil.getValue(viewModel.uiState)
        assertEquals(initialUiModel, uiState)
    }

    @Test
    fun signup() = runBlocking {
        // Given a view model
        val viewModel = LoginViewModel(loginRepo, provideFakeCoroutinesContextProvider())

        // When signing up
        viewModel.signup()

        // Then an open url event is emitted
        val url = LiveDataTestUtil.getValue(viewModel.openUrl)
        assertNotNull(url)
    }
}
