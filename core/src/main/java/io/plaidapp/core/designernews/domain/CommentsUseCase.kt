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

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.model.Comment
import io.plaidapp.core.designernews.data.api.model.User
import io.plaidapp.core.designernews.data.users.UserRepository
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class CommentsUseCase(
    private val commentsWithCommentsWithRepliesUseCase: CommentsWithRepliesUseCase,
    private val userRepository: UserRepository,
    private val contextProvider: CoroutinesContextProvider
) {
    fun getComments(
        ids: List<Long>,
        onResult: (result: Result<List<Comment>>) -> Unit
    ) = launch(contextProvider.io) {
        val commentsWithRepliesResult = commentsWithCommentsWithRepliesUseCase.getCommentsWithReplies(ids)
        if (commentsWithRepliesResult is Result.Error) {
            withContext(contextProvider.main) {
                Result.Error(commentsWithRepliesResult.exception)
            }
            return@launch
        }
        val commentsWithReplies = (commentsWithRepliesResult as Result.Success).data
        val userIds = mutableSetOf<Long>()
        getUserIds(commentsWithReplies, userIds)

        val usersResult = userRepository.getUsers(userIds)
        val users = if (usersResult is Result.Success) {
            usersResult.data
        } else {
            emptySet()
        }
        withContext(contextProvider.main) {
            onResult(Result.Success(createComments(commentsWithReplies, users)))
        }
    }

    private fun getUserIds(comments: List<CommentWithReplies>, userIds: MutableSet<Long>) {
        comments.map {
            userIds.add(it.userId)
            getUserIds(it.replies, userIds)
        }
    }

    private fun createComments(
        commentsWithReplies: List<CommentWithReplies>,
        users: Set<User>
    ): List<Comment> {
        val userMapping = users.map { it.id to it }.toMap()
        return match(commentsWithReplies, userMapping)
    }

    private fun match(
        commentsWithReplies: List<CommentWithReplies>,
        users: Map<Long, User>
    ): List<Comment> {
        val comments = mutableListOf<Comment>()
        for (comment in commentsWithReplies) {
            comments.add(
                    matchCommentWithRepliesAndUser(
                            comment,
                            match(comment.replies, users),
                            users
                    )
            )
        }
        return comments
    }

    private fun matchCommentWithRepliesAndUser(
        comment: CommentWithReplies,
        replies: List<Comment>,
        users: Map<Long, User>
    ): Comment {
        val user = users[comment.userId]
        return Comment(
                comment.id,
                comment.parentId,
                comment.body,
                comment.createdAt,
                comment.depth,
                comment.upvotesCount,
                replies,
                comment.userId,
                user?.displayName,
                user?.portraitUrl,
                false
        )
    }

    private fun matchCommentsWithRepliesAndUsers(
        comments: List<CommentWithReplies>,
        replies: List<Comment>,
        users: Map<Long, User>
    ): List<Comment> {
        val commentReplyMapping = replies.groupBy { it.parentCommentId }
        // for every comment construct the CommentWithReplies based on the comment properties and
        // the list of replies
        return comments.mapNotNull {
            val replies = commentReplyMapping[it.id]
            val user = users[it.userId]
            var comment: Comment? = null
            if (replies != null) {
                comment = Comment(
                        it.id,
                        it.parentId,
                        it.body,
                        it.createdAt,
                        it.depth,
                        it.upvotesCount,
                        replies,
                        it.userId,
                        user?.displayName,
                        user?.portraitUrl,
                        false
                )
            }
            comment
        }
    }
}