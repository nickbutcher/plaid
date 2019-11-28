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

package io.plaidapp.core.producthunt.data

import io.plaidapp.core.data.Result
import io.plaidapp.core.producthunt.data.api.ProductHuntService
import io.plaidapp.core.producthunt.data.api.model.GetPostsResponse
import io.plaidapp.core.util.safeApiCall
import java.io.IOException
import javax.inject.Inject

/**
 * Works with the Product Hunt API to get data.
 */
class ProductHuntRemoteDataSource @Inject constructor(private val service: ProductHuntService) {

    /**
     * Load Product Hunt data for a specific page.
     */
    suspend fun loadData(page: Int) = safeApiCall(
            call = { requestData(page) },
            errorMessage = "Error loading ProductHunt data"
    )

    private suspend fun requestData(page: Int): Result<GetPostsResponse> {
        val response = service.getPostsAsync(page)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }
        return Result.Error(IOException("Error loading ProductHunt data " +
                "${response.code()} ${response.message()}"))
    }
}
