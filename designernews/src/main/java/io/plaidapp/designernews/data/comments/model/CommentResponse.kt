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

package io.plaidapp.designernews.data.comments.model

import com.google.gson.annotations.SerializedName
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.designernews.domain.model.CommentWithReplies
import java.util.Date

fun CommentResponse.toCommentWithNoReplies(user: LoggedInUser) = Comment(
    id = id,
    parentCommentId = links.parentComment,
    body = body,
    createdAt = created_at,
    depth = depth,
    upvotesCount = links.commentUpvotes.size,
    userId = user.id,
    userDisplayName = user.displayName,
    userPortraitUrl = user.portraitUrl,
    upvoted = false
)

fun CommentResponse.toCommentsWithReplies(
    replies: List<CommentWithReplies>
) = CommentWithReplies(
    id = id,
    parentId = links.parentComment,
    body = body,
    createdAt = created_at,
    depth = depth,
    upvotesCount = links.commentUpvotes.size,
    userId = links.userId,
    storyId = links.story,
    replies = replies
)

/**
 * Models a comment on a designer news story response.
 */
data class CommentResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("body") val body: String,
    @SerializedName("created_at") val created_at: Date,
    @SerializedName("depth") val depth: Int = 0,
    @SerializedName("vote_count") var vote_count: Int = 0,
    @SerializedName("links") val links: CommentLinksResponse
)
