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
import io.plaidapp.core.designernews.data.api.model.CommentResponse
import io.plaidapp.core.designernews.data.comments.CommentsRepository
import java.io.IOException
import java.util.Date

class CommentsWithRepliesUseCase(private val commentsRepository: CommentsRepository) {

    /**
     * Get all comments and their replies. If we get an error on any reply depth level, ignore it
     * and just use the comments retrieved until that point.
     */
    suspend fun getCommentsWithReplies(parentIds: List<Long>): Result<List<CommentWithReplies>> {
        val replies = mutableListOf<List<CommentResponse>>()
        // get the first level of comments
        var result = commentsRepository.getComments(parentIds)
        // as long as we could get comments or replies to comments
        while (result is Result.Success) {
            val newReplies = result.data
            // add the replies
            replies.add(newReplies)
            // check if we have another level of replies
            val nextRepliesIds = newReplies.flatMap { comment -> comment.links.comments }
            if (!nextRepliesIds.isEmpty()) {
                result = commentsRepository.getComments(nextRepliesIds)
            } else {
                // we don't have any other level of replies match the replies to the comments
                // they belong to and return the first level of comments.
                if (replies.isNotEmpty()) {
                    return Result.Success(matchComments(replies))
                }
            }
        }
        // the last request was unsuccessful
        // if we already got some comments and replies, then use that data and ignore the error
        return if (replies.isNotEmpty()) {
            Result.Success(matchComments(replies))
        } else if (result is Result.Error) {
            result
        } else {
            Result.Error(IOException("Unable to get comments"))
        }
    }

    /**
     * Build up the replies tree, by matching the replies from lower levels to the level above they
     * belong to
     */
    private fun matchComments(comments: List<List<CommentResponse>>): List<CommentWithReplies> {
        var commentsWithReplies = emptyList<CommentWithReplies>()
        for (index in comments.size - 1 downTo 0) {
            commentsWithReplies = matchCommentsWithReplies(comments[index], commentsWithReplies)
        }
        return commentsWithReplies
    }

    private fun matchCommentsWithReplies(
        comments: List<CommentResponse>,
        replies: List<CommentWithReplies>
    ): List<CommentWithReplies> {
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
)