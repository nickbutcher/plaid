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

package io.plaidapp.dribbble.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import io.plaidapp.dribbble.ui.shot.DribbbleShotViewModel

/**
 * Factory for Dribbble [ViewModel]s
 */
class DribbbleViewModelFactory(
    private val contextProvider: CoroutinesContextProvider,
    private val getShareShotInfoUseCase: GetShareShotInfoUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DribbbleShotViewModel::class.java)) {
            return DribbbleShotViewModel(
                contextProvider,
                getShareShotInfoUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
