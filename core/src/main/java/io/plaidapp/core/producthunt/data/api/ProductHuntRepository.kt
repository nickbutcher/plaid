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

import io.plaidapp.core.data.prefs.SourceManager
import io.plaidapp.core.producthunt.data.api.model.Post
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductHuntRepository(private val service: ProductHuntService) {
    private val inflight: MutableMap<String, Call<*>> = HashMap()

    fun loadProductHuntData(
        page: Int,
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        val postsCall = service.getPosts(page)
        postsCall.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    onSuccess(result)
                } else {
                    onError("Unable to load Product Hunt data")
                }

                inflight.remove(SourceManager.SOURCE_PRODUCT_HUNT)
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                onError("Unable to load Product Hunt data ${t.message}")
                inflight.remove(SourceManager.SOURCE_PRODUCT_HUNT)
            }
        })
        inflight[SourceManager.SOURCE_PRODUCT_HUNT] = postsCall
    }

    fun cancelAllRequests() {
        for (request in inflight.values) request.cancel()
    }

    companion object {
        @Volatile
        private var INSTANCE: ProductHuntRepository? = null

        fun getInstance(service: ProductHuntService): ProductHuntRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                        ?: ProductHuntRepository(service).also { INSTANCE = it }
            }
        }
    }
}
