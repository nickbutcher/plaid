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

import io.plaidapp.base.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Repository for Designer News comments. Works with the [DesignerNewsCommentsRemoteDataSource] to
 * get the data.
 */
class DesignerNewsCommentsRepository(
    private val remoteDataSource: DesignerNewsCommentsRemoteDataSource,
    private val uiContext: CoroutineContext = UI,
    private val ioContext: CoroutineContext = CommonPool
) {

    /**
     * Gets comments, together will all the replies from the API. The successful result is
     * delivered to [onSuccess]. In case of failure the error is delivered to [onError], on the
     * [uiContext].
     */
    fun getComments(
        ids: List<Long>,
        onSuccess: (comments: List<Comment>) -> Unit,
        onError: (error: String) -> Unit
    ) {
        launch(uiContext) {
            // request comments and await until the result is received.
            val comments = getAllComments(ids)
            if (comments != null && comments.isNotEmpty()) {
                for (index: Int in comments.size - 1 downTo 1) {
                    matchCommentsWithReplies(comments[index - 1], comments[index])
                }
                onSuccess(comments[0])
            } else {
                onError("Unable to get comments")
            }
        }
    }

    /**
     * Get all comments and their replies, on the [ioContext].
     */
    private suspend fun getAllComments(
        parentIds: List<Long>
    ): List<List<Comment>>? {
        return withContext(ioContext) {
            val replies = mutableListOf<List<Comment>>()
            var newReplies = remoteDataSource.getComments(parentIds).await()
            while (newReplies != null) {
                replies.add(newReplies)
                val nextRepliesIds = newReplies.flatMap { comment -> comment.links.comments }
                newReplies = if (!nextRepliesIds.isEmpty()) {
                    remoteDataSource.getComments(nextRepliesIds).await()
                } else {
                    null
                }
            }
            replies
        }
    }

    private fun matchCommentsWithReplies(
        comments: List<Comment>,
        replies: List<Comment>
    ): List<Comment> {
        replies.map { reply ->
            comments.filter { comment -> comment.id == reply.links.parentComment }
                    .map { comment -> comment.addComment(reply) }
        }
        return comments
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRepository? = null

        fun getInstance(
            remoteDataSource: DesignerNewsCommentsRemoteDataSource
        ): DesignerNewsCommentsRepository {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE
                                ?: DesignerNewsCommentsRepository(
                                        remoteDataSource
                                ).also { INSTANCE = it }
                    }
        }
    }
}
