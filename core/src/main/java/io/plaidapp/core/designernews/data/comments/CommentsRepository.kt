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
import io.plaidapp.core.designernews.data.comments.model.CommentResponse

/**
 * Class that knows how to get and store Designer News comments.
 */
class CommentsRepository(private val remoteDataSource: CommentsRemoteDataSource) {

    /**
     * Get the list of [CommentResponse]s corresponding to [ids]
     */
    suspend fun getComments(ids: List<Long>): Result<List<CommentResponse>> {
        return remoteDataSource.getComments(ids)
    }

    companion object {
        @Volatile
        private var INSTANCE: CommentsRepository? = null

        fun getInstance(
            remoteDataSource: CommentsRemoteDataSource
        ): CommentsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CommentsRepository(remoteDataSource).also { INSTANCE = it }
            }
        }
    }
}
