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

package io.plaidapp.designernews.domain

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.designernews.data.votes.VotesRepository

/**
 * Use case that based on a comment id and on the id of the logged in user upvotes a comment.
 */
class UpvoteCommentUseCase(
    private val loginRepository: LoginRepository,
    private val votesRepository: VotesRepository
) {
    suspend operator fun invoke(commentId: Long): Result<Unit> {
        val userId = loginRepository.user?.id
            ?: throw IllegalStateException("User must be logged in to upvote a comment")
        return votesRepository.upvoteComment(commentId, userId)
    }
}
