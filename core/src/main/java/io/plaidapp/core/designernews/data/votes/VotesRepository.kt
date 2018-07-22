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

package io.plaidapp.core.designernews.data.votes

import io.plaidapp.core.data.Result

/**
 * Repository for Designer News votes.
 */
class VotesRepository(private val remoteDataSource: VotesRemoteDataSource) {

    suspend fun upvoteStory(storyId: Long, userId: Long): Result<Unit> {
        // TODO save the response in the database
        return remoteDataSource.upvoteStory(storyId, userId)
    }

    suspend fun upvoteComment(commentId: Long, userId: Long): Result<Unit> {
        // TODO save the response in the database
        return remoteDataSource.upvoteComment(commentId, userId)
    }

    companion object {
        @Volatile
        private var INSTANCE: VotesRepository? = null

        fun getInstance(remoteDataSource: VotesRemoteDataSource): VotesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VotesRepository(remoteDataSource).also { INSTANCE = it }
            }
        }
    }
}
