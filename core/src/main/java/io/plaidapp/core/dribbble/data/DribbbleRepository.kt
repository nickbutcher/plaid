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

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.search.DribbbleSearchRemoteDataSource
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * Repository class that handles working with Dribbble.
 */
class DribbbleRepository(
    private val remoteDataSource: DribbbleSearchRemoteDataSource,
    private val contextProvider: CoroutinesContextProvider
) {

    fun search(
        query: String,
        page: Int,
        onResult: (Result<List<Shot>>) -> Unit
    ) = launch(contextProvider.io) {
        val result = remoteDataSource.search(query, page)
        withContext(contextProvider.main) { onResult(result) }
    }

    companion object {
        @Volatile private var INSTANCE: DribbbleRepository? = null

        fun getInstance(
            remoteDataSource: DribbbleSearchRemoteDataSource,
            contextProvider: CoroutinesContextProvider
        ): DribbbleRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DribbbleRepository(remoteDataSource, contextProvider).also { INSTANCE = it }
            }
        }
    }
}
