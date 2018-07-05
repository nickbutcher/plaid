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

package io.plaidapp.core.designernews.data.api.votes

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.api.votes.model.UpvoteRequest
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.IOException

/**
 * Repository for Designer News votes.
 */
class DesignerNewsVotesRepository(
    private val service: DesignerNewsService,
    private val contextProvider: CoroutinesContextProvider
) {
    fun upvoteStory(
        id: Long,
        userId: Long,
        onResult: (result: Result<Unit>) -> Unit
    ) = launch(contextProvider.main) {
        val request = UpvoteRequest(id, userId)
        // right now we just care whether the response is successful or not.
        // TODO save the response in the database
        val response = withContext(contextProvider.io) { service.upvoteStoryV2(request) }.await()
        if (response.isSuccessful) {
            onResult(Result.Success(Unit))
            return@launch
        } else {
            onResult(Result.Error(IOException(
                    "Unable to upvote story ${response.code()} ${response.errorBody()?.string()}")))
            return@launch
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsVotesRepository? = null

        fun getInstance(
            service: DesignerNewsService,
            contextProvider: CoroutinesContextProvider
        ): DesignerNewsVotesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                        ?: DesignerNewsVotesRepository(service, contextProvider)
                                .also { INSTANCE = it }
            }
        }
    }
}
