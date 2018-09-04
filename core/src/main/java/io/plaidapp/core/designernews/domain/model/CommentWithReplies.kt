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

package io.plaidapp.core.designernews.domain.model

import io.plaidapp.core.designernews.data.users.model.User
import java.util.Date

fun CommentWithReplies.toComment(
    user: User?
) = Comment(
    id = id,
    parentCommentId = parentId,
    body = body,
    createdAt = createdAt,
    depth = depth,
    upvotesCount = upvotesCount,
    userId = userId,
    userDisplayName = user?.displayName,
    userPortraitUrl = user?.portraitUrl,
    upvoted = false
)

/**
 * Models a comment with replies.
 */
data class CommentWithReplies(
    val id: Long,
    val parentId: Long?,
    val body: String,
    val createdAt: Date,
    val depth: Int = 0,
    val upvotesCount: Int = 0,
    val userId: Long,
    val storyId: Long,
    val replies: List<CommentWithReplies> = emptyList()
) {
    /**
     * @return a flattened, recursive sequence of this [CommentWithReplies] and all its nested
     * [replies].
     */
    val flattenWithReplies: Sequence<CommentWithReplies>
        get() = sequenceOf(this) + replies.asSequence().flatMap(CommentWithReplies::flattenWithReplies)
}
