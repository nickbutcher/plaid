/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.core.producthunt.domain

import io.plaidapp.core.data.Result
import io.plaidapp.core.producthunt.data.api.ProductHuntRepository
import io.plaidapp.core.producthunt.data.api.model.Post
import io.plaidapp.core.producthunt.data.api.model.toPost
import javax.inject.Inject

/**
 * Class that knows how to load posts
 */
class LoadPostsUseCase @Inject constructor(
    private val productHuntRepository: ProductHuntRepository
) {

    /**
     * Load Product Hunt data for a specific page and return the result either in onSuccess or in
     * onError
     */
    suspend operator fun invoke(
        page: Int
    ): Result<List<Post>> {
        val result = productHuntRepository.loadPosts(page)
        return when (result) {
            is Result.Success -> {
                val posts = result.data.posts.map { it.toPost() }
                Result.Success(posts)
            }
            is Result.Error -> result
        }
    }
}
