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
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.votes.model.UpvoteCommentRequest
import io.plaidapp.core.designernews.data.votes.model.UpvoteStoryRequest
import java.io.IOException

/**
 * Class that works with the Designer News API to up/down vote comments and stories
 */
class VotesRemoteDataSource(private val service: DesignerNewsService) {

    suspend fun upvoteStory(storyId: Long, userId: Long): Result<Unit> {
        val request = UpvoteStoryRequest(storyId, userId)
        // right now we just care whether the response is successful or not.
        val response = service.upvoteStoryV2(request).await()
        return if (response.isSuccessful) {
            Result.Success(Unit)
        } else {
            Result.Error(IOException("Unable to upvote story ${response.code()} ${response.errorBody()?.string()}"))
        }
    }

    suspend fun upvoteComment(commentId: Long, userId: Long): Result<Unit> {
        val request = UpvoteCommentRequest(commentId, userId)
        // right now we just care whether the response is successful or not.
        val response = service.upvoteComment(request).await()
        return if (response.isSuccessful) {
            Result.Success(Unit)
        } else {
            Result.Error(IOException("Unable to upvote comment ${response.code()} ${response.errorBody()?.string()}"))
        }
    }
}
