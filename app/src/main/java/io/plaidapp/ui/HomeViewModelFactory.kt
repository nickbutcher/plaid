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

package io.plaidapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.plaidapp.core.data.DataManager
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.login.LoginRepository
import javax.inject.Inject

/**
 * Factory to create [HomeViewModel]
 */
class HomeViewModelFactory @Inject constructor(
    private val dataManager: DataManager,
    private val loginRepository: LoginRepository,
    private val sourcesRepository: SourcesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass != HomeViewModel::class.java) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return HomeViewModel(dataManager, loginRepository, sourcesRepository) as T
    }
}
