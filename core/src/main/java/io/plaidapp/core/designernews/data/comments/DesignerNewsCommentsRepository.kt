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

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.model.Comment
import io.plaidapp.core.designernews.data.api.model.CommentResponse
import io.plaidapp.core.designernews.data.api.model.User
import io.plaidapp.core.designernews.data.users.UserRepository
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.IOException

/**
 * Repository for Designer News comments. Works with the [DesignerNewsCommentsRemoteDataSource] to
 * get the data.
 */
class DesignerNewsCommentsRepository(
    private val remoteDataSource: DesignerNewsCommentsRemoteDataSource,
    private val userRepository: UserRepository,
    private val contextProvider: CoroutinesContextProvider
) {

    /**
     * Gets comments, together will all the replies from the API. The result is
     * delivered to [onResult].
     * TODO since this method is called from java, for now, let this method handle the context
     * switching. This should be moved to a point closer to the UI, ensuring that all the
     * business logic is away from the main context
     */
    fun getComments(
        ids: List<Long>,
        onResult: (result: Result<List<Comment>>) -> Unit
    ) = launch(contextProvider.main) {
        // request comments and await until the result is received.
        val result = withContext(contextProvider.io) { getAllCommentsWithUsers(ids) }
        onResult(result)
    }

    /**
     * Get all comments and their replies. If we get an error on any reply depth level, ignore it
     * and just use the comments retrieved until that point.
     */
    private suspend fun getAllCommentsWithUsers(parentIds: List<Long>): Result<List<Comment>> {
        val replies = mutableListOf<List<CommentResponse>>()
        // get the first level of comments
        var result = remoteDataSource.getComments(parentIds)
        // as long as we could get comments or replies to comments
        while (result is Result.Success) {
            val newReplies = result.data
            // add the replies
            replies.add(newReplies)
            // check if we have another level of replies
            val nextRepliesIds = newReplies.flatMap { comment -> comment.links.comments }
            if (!nextRepliesIds.isEmpty()) {
                result = remoteDataSource.getComments(nextRepliesIds)
            } else {
                // we don't have any other level of replies match the replies to the comments
                // they belong to and return the first level of comments.
                if (replies.isNotEmpty()) {
                    return buildCommentsWithUsers(replies)
                }
            }
        }
        // the last request was unsuccessful
        // if we already got some comments and replies, then use that data and ignore the error
        return if (replies.isNotEmpty()) {
            buildCommentsWithUsers(replies)
        } else if (result is Result.Error) {
            result
        } else {
            Result.Error(IOException("Unable to get comments"))
        }
    }

    private fun buildCommentsWithRepliesAndUser(
        comments: List<CommentResponse>,
        replies: List<Comment>,
        users: Map<Long, User>
    ): List<Comment> {
        val mapping = replies.groupBy { it.parentComment }
        return comments.map {
            val commentReplies = mapping[it.id]
            val user = users[it.links.userId]
            Comment(
                    it.id,
                    it.links.parentComment,
                    it.body,
                    it.created_at,
                    it.depth,
                    it.vote_count,
                    commentReplies.orEmpty(),
                    user?.id,
                    user?.displayName,
                    user?.portraitUrl,
                    false)
        }
    }

    private suspend fun buildCommentsWithUsers(
        replies: List<List<CommentResponse>>
    ): Result<List<Comment>> {
        val usersResult = getUsersForComments(replies)
        // no users, no user data displayed.
        val userData = if (usersResult is Result.Success) {
            usersResult.data
        } else {
            emptyList()
        }
        return Result.Success(matchUsersWithComments(replies, userData))
    }

    private suspend fun getUsersForComments(
        comments: List<List<CommentResponse>>
    ): Result<List<User>> {
        // get all the user ids corresponding to the comments
        val userIds = mutableSetOf<Long>()
        comments.map { userIds.addAll(it.map { it.links.userId }) }

        return userRepository.getUsers(userIds)
    }

    private fun matchUsersWithComments(
        comments: List<List<CommentResponse>>,
        users: List<User>
    ): List<Comment> {
        val usersMap = users.map { it.id to it }.toMap()

        var lastReplies = emptyList<Comment>()
        for (index in comments.size - 1 downTo 0) {
            lastReplies = buildCommentsWithRepliesAndUser(comments[index], lastReplies, usersMap)
        }
        return lastReplies
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRepository? = null

        fun getInstance(
            remoteDataSource: DesignerNewsCommentsRemoteDataSource,
            userRepository: UserRepository,
            contextProvider: CoroutinesContextProvider
        ): DesignerNewsCommentsRepository {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE
                                ?: DesignerNewsCommentsRepository(
                                        remoteDataSource,
                                        userRepository,
                                        contextProvider
                                ).also { INSTANCE = it }
                    }
        }
    }
}
