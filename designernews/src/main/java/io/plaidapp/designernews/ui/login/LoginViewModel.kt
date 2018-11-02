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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.plaidapp.R
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.util.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * View Model for [LoginActivity]
 */
private const val signupUrl = "https://www.designernews.co/users/new"

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    private val parentJob = Job()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)
    private var loginJob: Job? = null

    private val _uiState = MutableLiveData<LoginUiModel>()
    val uiState: LiveData<LoginUiModel>
        get() = _uiState

    private val _openUrl = MutableLiveData<Event<String>>()
    val openUrl: LiveData<Event<String>>
        get() = _openUrl

    init {
        // at view model initiation, the login is not valid so the login button should be disabled
        enableLogin(false)
    }

    fun login(username: String, password: String) {
        // only allow one login at a time
        if (loginJob?.isActive == true) {
            return
        }
        loginJob = launchLogin(username, password)
    }

    private fun launchLogin(username: String, password: String): Job {
        return scope.launch(dispatcherProvider.computation) {
            if (!isLoginValid(username, password)) {
                return@launch
            }
            withContext(dispatcherProvider.main) { showLoading() }
            val result = loginRepository.login(username, password)

            withContext(dispatcherProvider.main) {
                if (result is Result.Success) {
                    val user = result.data
                    emitUiState(
                        showSuccess = Event(
                            LoginResultUiModel(
                                user.displayName.toLowerCase(),
                                user.portraitUrl
                            )
                        )
                    )
                } else {
                    emitUiState(
                        showError = Event(R.string.login_failed),
                        enableLoginButton = true
                    )
                }
            }
        }
    }

    private fun showLoading() {
        emitUiState(showProgress = true)
    }

    override fun onCleared() {
        super.onCleared()
        // when the VM is destroyed, cancel the running job.
        parentJob.cancel()
    }

    fun signup() {
        _openUrl.value = Event(signupUrl)
    }

    fun loginDataChanged(username: String, password: String) {
        enableLogin(isLoginValid(username, password))
    }

    private fun isLoginValid(username: String, password: String): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }

    private fun enableLogin(enabled: Boolean) {
        emitUiState(enableLoginButton = enabled)
    }

    private fun emitUiState(
        showProgress: Boolean = false,
        showError: Event<Int>? = null,
        showSuccess: Event<LoginResultUiModel>? = null,
        enableLoginButton: Boolean = false
    ) {
        val uiModel = LoginUiModel(showProgress, showError, showSuccess, enableLoginButton)
        _uiState.value = uiModel
    }
}

/**
 * UI model for [LoginActivity]
 */
data class LoginUiModel(
    val showProgress: Boolean,
    val showError: Event<Int>?,
    val showSuccess: Event<LoginResultUiModel>?,
    val enableLoginButton: Boolean
)

/**
 * UI Model for login success
 */
data class LoginResultUiModel(
    val displayName: String,
    val portraitUrl: String?
)
