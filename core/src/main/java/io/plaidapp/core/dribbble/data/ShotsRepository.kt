/*
 * Copyright 2018 Google LLC.
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

import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource

/**
 * Repository class that handles working with Dribbble.
 */
class ShotsRepository constructor(private val remoteDataSource: SearchRemoteDataSource) {

    private val shotCache = mutableMapOf<Long, Shot>()

    suspend fun search(query: String, page: Int): Result<List<Shot>> {
        val result = remoteDataSource.search(query, page)
        if (result is Result.Success) {
            cache(result.data)
        }
        return result
    }

    fun getShot(id: Long): Result<Shot> {
        val shot = shotCache[id]
        return if (shot != null) {
            Result.Success(shot)
        } else {
            Result.Error(IllegalStateException("Shot $id not cached"))
        }
    }

    private fun cache(shots: List<Shot>) {
        shots.associateTo(shotCache) { it.id to it }
    }

    companion object {
        @Volatile
        private var INSTANCE: ShotsRepository? = null

        fun getInstance(remoteDataSource: SearchRemoteDataSource): ShotsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ShotsRepository(remoteDataSource)
                    .also { INSTANCE = it }
            }
        }
    }
}
