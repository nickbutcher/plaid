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

package io.plaidapp.core.producthunt.data.api

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.producthunt.data.ProductHuntRemoteDataSource
import io.plaidapp.core.producthunt.data.api.model.Post
import io.plaidapp.core.util.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Class that knows how to get Product Hunt posts
 */
class ProductHuntRepository(
    private val remoteDataSource: ProductHuntRemoteDataSource,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    private val parentJob = Job()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)

    /**
     * Load Product Hunt data for a specific page and return the result either in onSuccess or in
     * onError
     */
    fun loadProductHuntData(
        page: Int,
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        launchLoad(page, onSuccess, onError)
    }

    private fun launchLoad(
        page: Int,
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) = scope.launch(dispatcherProvider.io) {
        val result = remoteDataSource.loadData(page)
        withContext(dispatcherProvider.main) {
            when (result) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error -> onError(result.exception.toString())
            }.exhaustive
        }
    }

    fun cancelAllRequests() {
        parentJob.cancelChildren()
    }

    companion object {
        @Volatile
        private var INSTANCE: ProductHuntRepository? = null

        fun getInstance(
            remoteDataSource: ProductHuntRemoteDataSource,
            dispatcherProvider: CoroutinesDispatcherProvider
        ): ProductHuntRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProductHuntRepository(remoteDataSource, dispatcherProvider)
                                .also { INSTANCE = it }
            }
        }
    }
}
