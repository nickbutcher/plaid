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

package io.plaidapp.core.designernews.domain

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.model.Comment
import io.plaidapp.core.designernews.data.api.model.User
import io.plaidapp.core.designernews.data.users.UserRepository

class CommentsUseCase(
    private val commentsWithCommentsWithRepliesUseCase: CommentsWithRepliesUseCase,
    private val userRepository: UserRepository
) {
    suspend fun getComments(ids: List<Long>): Result<List<Comment>> {
        val commentsWithRepliesResult = commentsWithCommentsWithRepliesUseCase.getCommentsWithReplies(ids)
        if (commentsWithRepliesResult is Result.Error) {
            return Result.Error(commentsWithRepliesResult.exception)
        }
        val commentsWithReplies = (commentsWithRepliesResult as Result.Success).data
        val userIds = mutableSetOf<Long>()
        getUserIds(commentsWithReplies, userIds)

        val users = userRepository.getUsers(userIds)
    }

    fun getUserIds(comments: List<CommentWithReplies>, userIds: MutableSet<Long>) {
        comments.map {
            userIds.add(it.userId)
            getUserIds(it.replies, userIds)
        }
    }

    fun createComments(
        commentsWithReplies: List<CommentWithReplies>,
        users: Set<User>
    ): List<Comment> {
        val userMapping = users.map { it.id to it }.toMap()
    }

    private fun matchCommentsWithRepliesAndUsers(
        comments: List<CommentWithReplies>,
        replies: List<Comment>,
        users: Map<Long, User>
    ): List<Comment> {
        val commentReplyMapping = replies.groupBy { it.parentId }
        // for every comment construct the CommentWithReplies based on the comment properties and
        // the list of replies
        return comments.mapNotNull {
            val replies = commentReplyMapping[it.id]
            var comment: CommentWithReplies? = null
            if (replies != null) {
                comment = CommentWithReplies(
                        it.id,
                        it.links.parentComment,
                        it.body,
                        it.created_at,
                        it.depth,
                        it.links.commentUpvotes.size,
                        it.links.userId,
                        it.links.story,
                        replies
                )
            }
            comment
        }
    }
}