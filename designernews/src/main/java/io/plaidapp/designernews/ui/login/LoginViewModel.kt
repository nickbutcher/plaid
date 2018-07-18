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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.util.exhaustive
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

/**
 * View Model for [LoginActivity]
 * TODO move the rest of the logic from activity here.
 * TODO keep this in core for now, to be moved to designernews module
 */
class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val contextProvider: CoroutinesContextProvider
) : ViewModel() {

    private var currentJob: Job? = null

    private val _uiState = MutableLiveData<Result<LoginUiModel>>()
    val uiState: LiveData<Result<LoginUiModel>>
        get() = _uiState

    fun login(username: String, password: String) {
        // only allow one login at a time
        if (currentJob?.isActive == true) {
            return
        }
        currentJob = launchLogin(username, password)
    }

    private fun launchLogin(username: String, password: String) = launch(contextProvider.io) {
        _uiState.postValue(Result.Loading)
        val result = loginRepository.login(username, password)

        when (result) {
            is Result.Success -> {
                val user = result.data
                val uiModel = LoginUiModel(
                    user.displayName.toLowerCase(),
                    user.portraitUrl
                )
                _uiState.postValue(Result.Success(uiModel))
            }
            is Result.Error -> _uiState.postValue(result)
            is Result.Loading -> {
                /* we ignore the loading state */
            }
        }.exhaustive
    }

    override fun onCleared() {
        super.onCleared()
        // when the VM is destroyed, cancel the running job.
        currentJob?.cancel()
    }
}

/**
 * UI model for [LoginActivity]
 */
data class LoginUiModel(val displayName: String, val portraitUrl: String?)
