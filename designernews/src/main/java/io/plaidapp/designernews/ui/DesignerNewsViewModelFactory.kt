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

package io.plaidapp.designernews.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.designernews.ui.login.LoginViewModel

/**
 * Factory for Designer News [ViewModel]s
 */
class DesignerNewsViewModelFactory(
    private val loginRepository: LoginRepository,
    private val contextProvider: CoroutinesContextProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(loginRepository, contextProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
