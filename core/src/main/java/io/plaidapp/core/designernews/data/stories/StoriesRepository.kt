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

package io.plaidapp.core.designernews.data.stories

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.model.StoryResponse

/**
 * Repository class that handles work with Designer News Stories.
 */
class StoriesRepository(private val remoteDataSource: StoriesRemoteDataSource) {

    private val cache = mutableMapOf<Long, StoryResponse>()

    suspend fun loadStories(page: Int) = getData { remoteDataSource.loadStories(page) }

    suspend fun search(query: String, page: Int) = getData { remoteDataSource.search(query, page) }

    private suspend fun getData(
        request: suspend () -> Result<List<StoryResponse>>
    ): Result<List<StoryResponse>> {
        val result = request()
        if (result is Result.Success) {
            cache(result.data)
        }
        return result
    }

    fun getStory(id: Long): Result<StoryResponse> {
        val story = cache[id]
        return if (story != null) {
            Result.Success(story)
        } else {
            Result.Error(IllegalStateException("Story $id not cached"))
        }
    }

    private fun cache(data: List<StoryResponse>) {
        data.associateTo(cache) { it.id to it }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoriesRepository? = null

        fun getInstance(remoteDataSource: StoriesRemoteDataSource): StoriesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoriesRepository(remoteDataSource).also {
                    INSTANCE = it
                }
            }
        }
    }
}
