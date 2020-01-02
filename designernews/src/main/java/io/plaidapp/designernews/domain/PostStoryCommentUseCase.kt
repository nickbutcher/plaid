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
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.util.exhaustive
import io.plaidapp.designernews.data.comments.CommentsRepository
import io.plaidapp.designernews.data.comments.model.toCommentWithNoReplies
import javax.inject.Inject

/**
 * Post a comment to a story on behalf of the logged in user.
 */
class PostStoryCommentUseCase @Inject constructor(
    private val commentsRepository: CommentsRepository,
    private val loginRepository: LoginRepository
) {

    suspend operator fun invoke(body: String, storyId: Long): Result<Comment> {
        checkNotNull(loginRepository.user) {
            "User should be logged in, in order to post a comment"
        }
        val user = loginRepository.user!!
        val result = commentsRepository.postStoryComment(body, storyId, user.id)
        return when (result) {
            is Result.Success -> {
                val commentResponse = result.data
                return Result.Success(commentResponse.toCommentWithNoReplies(user))
            }
            is Result.Error -> result
        }.exhaustive
    }
}
