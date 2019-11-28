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

package io.plaidapp.core.dribbble.data.search

import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.search.DribbbleSearchService.Companion.PER_PAGE_DEFAULT
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource.SortOrder.RECENT
import io.plaidapp.core.util.safeApiCall
import java.io.IOException
import javax.inject.Inject

/**
 * Work with our fake Dribbble API to search for shots by query term.
 */
class SearchRemoteDataSource @Inject constructor(private val service: DribbbleSearchService) {

    suspend fun search(
        query: String,
        page: Int,
        sortOrder: SortOrder = RECENT,
        pageSize: Int = PER_PAGE_DEFAULT
    ) = safeApiCall(
        call = { requestSearch(query, page, sortOrder, pageSize) },
        errorMessage = "Error getting Dribbble data"
    )

    private suspend fun requestSearch(
        query: String,
        page: Int,
        sortOrder: SortOrder = RECENT,
        pageSize: Int = PER_PAGE_DEFAULT
    ): Result<List<Shot>> {
        val response = service.searchDeferred(query, page, sortOrder.sort, pageSize)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }
        return Result.Error(
            IOException("Error getting Dribbble data ${response.code()} ${response.message()}")
        )
    }

    enum class SortOrder(val sort: String) {
        POPULAR(""),
        RECENT("latest")
    }

    companion object {
        @Volatile
        private var INSTANCE: SearchRemoteDataSource? = null

        fun getInstance(service: DribbbleSearchService): SearchRemoteDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SearchRemoteDataSource(service).also { INSTANCE = it }
            }
        }
    }
}
