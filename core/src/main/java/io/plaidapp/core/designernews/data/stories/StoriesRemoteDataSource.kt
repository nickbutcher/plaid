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
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import java.io.IOException
import retrofit2.Response

/**
 * Data source class that handles work with Designer News API.
 */
class StoriesRemoteDataSource(private val service: DesignerNewsService) {

    suspend fun loadStories(page: Int): Result<List<StoryResponse>> {
        return try {
            val response = service.getStories(page)
            getResult(response = response, onError = {
                Result.Error(
                    IOException("Error getting stories ${response.code()} ${response.message()}")
                )
            })
        } catch (e: Exception) {
            Result.Error(IOException("Error getting stories", e))
        }
    }

    suspend fun search(query: String, page: Int): Result<List<StoryResponse>> {
        val queryWithoutPrefix =
            query.replace(DesignerNewsSearchSourceItem.DESIGNER_NEWS_QUERY_PREFIX, "")
        return try {
            val searchResults = service.search(queryWithoutPrefix, page)
            val ids = searchResults.body()
            if (searchResults.isSuccessful && !ids.isNullOrEmpty()) {
                val commaSeparatedIds = ids.joinToString(",")
                loadStories(commaSeparatedIds)
            } else {
                Result.Error(IOException("Error searching $queryWithoutPrefix"))
            }
        } catch (e: Exception) {
            Result.Error(IOException("Error searching $queryWithoutPrefix", e))
        }
    }

    private suspend fun loadStories(commaSeparatedIds: String): Result<List<StoryResponse>> {
        return try {
            val response = service.getStories(commaSeparatedIds)
            getResult(response = response, onError = {
                Result.Error(
                    IOException("Error getting stories ${response.code()} ${response.message()}")
                )
            })
        } catch (e: Exception) {
            Result.Error(IOException("Error getting stories", e))
        }
    }

    private inline fun getResult(
        response: Response<List<StoryResponse>>,
        onError: () -> Result.Error
    ): Result<List<StoryResponse>> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }
        return onError.invoke()
    }

    companion object {
        @Volatile
        private var INSTANCE: StoriesRemoteDataSource? = null

        fun getInstance(service: DesignerNewsService): StoriesRemoteDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoriesRemoteDataSource(service).also { INSTANCE = it }
            }
        }
    }
}
