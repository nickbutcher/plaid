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

package io.plaidapp.core.dribbble.data

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository class that handles working with Dribbble.
 */
class ShotsRepository constructor(
    private val remoteDataSource: SearchRemoteDataSource,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {

    private val parentJob = Job()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)

    private val inflight = mutableMapOf<String, Job>()
    private val shotCache = mutableMapOf<Long, Shot>()

    fun search(
        query: String,
        page: Int,
        onResult: (Result<List<Shot>>) -> Unit
    ) {
        val id = "$query::$page"
        inflight[id] = launchSearch(query, page, id, onResult)
    }

    fun getShot(id: Long): Result<Shot> {
        val shot = shotCache[id]
        return if (shot != null) {
            Result.Success(shot)
        } else {
            Result.Error(IllegalStateException("Shot $id not cached"))
        }
    }

    fun cancelAllSearches() {
        parentJob.cancelChildren()
        inflight.clear()
    }

    private fun launchSearch(
        query: String,
        page: Int,
        id: String,
        onResult: (Result<List<Shot>>) -> Unit
    ) = scope.launch(dispatcherProvider.io) {
        val result = remoteDataSource.search(query, page)
        inflight.remove(id)
        if (result is Result.Success) {
            cache(result.data)
        }
        withContext(dispatcherProvider.main) { onResult(result) }
    }

    private fun cache(shots: List<Shot>) {
        shots.associateTo(shotCache) { it.id to it }
    }

    companion object {
        @Volatile
        private var INSTANCE: ShotsRepository? = null

        fun getInstance(
            remoteDataSource: SearchRemoteDataSource,
            dispatcherProvider: CoroutinesDispatcherProvider
        ): ShotsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ShotsRepository(remoteDataSource, dispatcherProvider)
                    .also { INSTANCE = it }
            }
        }
    }
}
