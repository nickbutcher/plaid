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

package io.plaidapp.core.designernews.data.comments

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.comments.model.CommentResponse
import java.io.IOException

/**
 * Work with the Designer News API to get comments. The class knows how to construct the requests.
 */
class CommentsRemoteDataSource(private val service: DesignerNewsService) {

    /**
     * Get a list of comments based on ids from Designer News API.
     */
    suspend fun getComments(ids: List<Long>): Result<List<CommentResponse>> {
        val requestIds = ids.joinToString(",")
        val response = service.getComments(requestIds).await()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }
        return Result.Error(IOException("Error getting comments ${response.code()} ${response.message()}"))
    }

    companion object {
        @Volatile private var INSTANCE: CommentsRemoteDataSource? = null

        fun getInstance(service: DesignerNewsService): CommentsRemoteDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CommentsRemoteDataSource(service).also { INSTANCE = it }
            }
        }
    }
}
