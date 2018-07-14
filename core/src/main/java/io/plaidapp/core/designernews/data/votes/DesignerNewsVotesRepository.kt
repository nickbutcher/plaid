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

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * Repository for Designer News votes.
 */
class DesignerNewsVotesRepository(
    private val remoteDataSource: VotesRemoteDataSource,
    private val contextProvider: CoroutinesContextProvider
) {
    fun upvoteStory(
        storyId: Long,
        userId: Long,
        onResult: (result: Result<Unit>) -> Unit
    ) = launch(contextProvider.io) {
        // TODO save the response in the database
        val response = remoteDataSource.upvoteStory(storyId, userId)
        withContext(contextProvider.main) { onResult(response) }
    }

    fun upvoteComment(
        commentId: Long,
        userId: Long,
        onResult: (result: Result<Unit>) -> Unit
    ) = launch(contextProvider.io) {
        // TODO save the response in the database
        val response = remoteDataSource.upvoteComment(commentId, userId)
        withContext(contextProvider.main) { onResult(response) }
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsVotesRepository? = null

        fun getInstance(
            remoteDataSource: VotesRemoteDataSource,
            contextProvider: CoroutinesContextProvider
        ): DesignerNewsVotesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DesignerNewsVotesRepository(remoteDataSource, contextProvider).also { INSTANCE = it }
            }
        }
    }
}
