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

package io.plaidapp.core.designernews.data.api.comments

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.model.Comment
import io.plaidapp.core.designernews.data.api.model.User
import io.plaidapp.core.designernews.data.users.UserRepository
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

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
        val result = withContext(contextProvider.io) { getAllComments(ids) }
        onResult(result)
    }

    /**
     * Get all comments and their replies. If we get an error on any reply depth level, ignore it
     * and just use the comments retrieved until that point.
     */
    private suspend fun getAllComments(parentIds: List<Long>): Result<List<Comment>> {
        val replies = mutableListOf<List<Comment>>()
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
                    fillUserData(replies)
                    matchComments(replies)
                    return Result.Success(replies[0])
                }
            }
        }
        // the last request was unsuccessful
        // if we already got some comments and replies, then use that data and ignore the error
        return if (replies.isNotEmpty()) {
            fillUserData(replies)
            matchComments(replies)
            Result.Success(replies[0])
        } else {
            // if we never get any data, then return the error
            result
        }
    }

    /**
     * Build up the replies tree, by matching the replies from lower levels to the level above they
     * belong to
     */
    private fun matchComments(comments: List<List<Comment>>) {
        for (index in comments.size - 1 downTo 1) {
            matchCommentsWithReplies(comments[index - 1], comments[index])
        }
    }

    private fun matchCommentsWithReplies(
        comments: List<Comment>,
        replies: List<Comment>
    ): List<Comment> {
        // for every reply, get the comment to which the reply belongs to and add it to the list
        // of replies for that comment
        replies.map { reply ->
            comments.find { it.id == reply.links.parentComment }?.addReply(reply)
        }
        return comments
    }

    private suspend fun fillUserData(replies: List<List<Comment>>) {
        // get all the user ids corresponding to the comments
        val userIds = mutableSetOf<Long>()
        replies.map { userIds.addAll(it.map { it.links.userId }) }
        // get the users
        val usersResult = userRepository.getUsers(userIds)
        // no users, no data displayed. Ignore the error case for now
        if (usersResult is Result.Success) {
            matchUsersWithComments(replies, usersResult.data)
        }
    }

    private fun matchUsersWithComments(
        comments: List<List<Comment>>,
        users: List<User>
    ) {
        val usersMap = users.map { it.id to it }.toMap()
        comments.map { replies ->
            replies.map {
                val user = usersMap[it.links.userId]
                it.user_display_name = user?.displayName
                it.user_portrait_url = user?.portraitUrl
            }
        }
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
