/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.designernews.domain

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.designernews.domain.model.CommentWithReplies
import io.plaidapp.core.designernews.domain.model.toComment
import io.plaidapp.designernews.data.users.UserRepository
import javax.inject.Inject

/**
 * Use case that builds [Comment]s based on comments with replies and users
 */
class GetCommentsWithRepliesAndUsersUseCase @Inject constructor(
    private val getCommentsWithReplies: GetCommentsWithRepliesUseCase,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(ids: List<Long>): Result<List<Comment>> {
        // Get the comments with replies
        val commentsWithRepliesResult = getCommentsWithReplies(ids)
        if (commentsWithRepliesResult is Result.Error) {
            return commentsWithRepliesResult
        }
        val commentsWithReplies = (commentsWithRepliesResult as? Result.Success)?.data.orEmpty()
        // get the ids of the users that posted comments
        val userIds = mutableSetOf<Long>()
        createUserIds(commentsWithReplies, userIds)

        // get the users
        val usersResult = userRepository.getUsers(userIds)
        val users = if (usersResult is Result.Success) {
            usersResult.data
        } else {
            emptySet()
        }
        // create the comments based on the comments with replies and users
        val comments = createComments(commentsWithReplies, users)
        return Result.Success(comments)
    }

    private fun createUserIds(comments: List<CommentWithReplies>, userIds: MutableSet<Long>) {
        comments.forEach {
            userIds.add(it.userId)
            createUserIds(it.replies, userIds)
        }
    }

    private fun createComments(
        commentsWithReplies: List<CommentWithReplies>,
        users: Set<User>
    ): List<Comment> {
        val userMapping = users.associateBy(User::id)
        return commentsWithReplies.asSequence()
                .flatMap(CommentWithReplies::flattenWithReplies)
                .map { it.toComment(userMapping[it.userId]) }
                .toList()
    }
}
