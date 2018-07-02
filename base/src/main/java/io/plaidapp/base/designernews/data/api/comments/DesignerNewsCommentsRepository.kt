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

package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.data.CoroutinesContextProvider
import io.plaidapp.base.data.api.Result
import io.plaidapp.base.data.api.isSuccessful
import io.plaidapp.base.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * Repository for Designer News comments. Works with the [DesignerNewsCommentsRemoteDataSource] to
 * get the data.
 */
class DesignerNewsCommentsRepository(
    private val remoteDataSource: DesignerNewsCommentsRemoteDataSource,
    private val contextProvider: CoroutinesContextProvider
) {

    /**
     * Gets comments, together will all the replies from the API. The result is
     * delivered to [onResult].
     */
    fun getComments(
        ids: List<Long>,
        onResult: (result: Result<List<Comment>?>) -> Unit
    ) =
            launch(contextProvider.main) {
                // request comments and await until the result is received.
                val result = withContext(contextProvider.io) { getAllComments(ids) }
                onResult(result)
            }

    /**
     * Get all comments and their replies. If we get an error on any reply depth level, ignore it
     * and just use the comments retrieved until that point.
     */
    private suspend fun getAllComments(
        parentIds: List<Long>
    ): Result<List<Comment>?> {
        val replies = mutableListOf<List<Comment>>()
        var result = remoteDataSource.getComments(parentIds)
        while (result.isSuccessful()) {
            val newReplies = (result as Result.Success).data
            if (newReplies != null) {
                replies.add(newReplies)
                val nextRepliesIds = newReplies.flatMap { comment -> comment.links.comments }
                if (!nextRepliesIds.isEmpty()) {
                    result = remoteDataSource.getComments(nextRepliesIds)
                } else {
                    if (replies.isEmpty()) {
                        return result
                    }
                    matchComments(replies)
                    return Result.Success(replies[0])
                }
            }
        }
        return if (result.isSuccessful() && replies.size > 0) {
            Result.Success(replies[0])
        } else {
            result
        }
    }

    private fun matchComments(comments: List<List<Comment>>) {
        for (index: Int in comments.size - 1 downTo 1) {
            matchCommentsWithReplies(comments[index - 1], comments[index])
        }
    }

    private fun matchCommentsWithReplies(
        comments: List<Comment>,
        replies: List<Comment>
    ): List<Comment> {
        replies.map { reply ->
            comments.filter { comment -> comment.id == reply.links.parentComment }
                    .map { comment -> comment.addReply(reply) }
        }
        return comments
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRepository? = null

        fun getInstance(
            remoteDataSource: DesignerNewsCommentsRemoteDataSource,
            contextProvider: CoroutinesContextProvider
        ): DesignerNewsCommentsRepository {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE
                                ?: DesignerNewsCommentsRepository(
                                        remoteDataSource,
                                        contextProvider
                                ).also { INSTANCE = it }
                    }
        }
    }
}
