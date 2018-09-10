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

package io.plaidapp.designernews

import io.plaidapp.core.designernews.data.comments.model.CommentLinksResponse
import io.plaidapp.core.designernews.data.comments.model.CommentResponse
import io.plaidapp.core.designernews.data.stories.model.StoryLinks
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.designernews.domain.model.CommentWithReplies
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.util.Date
import java.util.GregorianCalendar

/**
 * Test data for comments
 */
val createdDate: Date = GregorianCalendar(1997, 12, 28).time

val loggedInUser = LoggedInUser(
    id = 111L,
    firstName = "Plaicent",
    lastName = "van Plaid",
    displayName = "Plaicent van Plaid",
    portraitUrl = "www",
    upvotes = listOf(123L, 234L, 345L)
)

val user1 = User(
    id = 111L,
    firstName = "Plaicent",
    lastName = "van Plaid",
    displayName = "Plaicent van Plaid",
    portraitUrl = "www"
)

val user2 = User(
    id = 222L,
    firstName = "Plaude",
    lastName = "Pladon",
    displayName = "Plaude Pladon",
    portraitUrl = "www"
)

const val parentId = 1L

val links = CommentLinksResponse(
    userId = user1.id,
    story = 999L,
    parentComment = parentId
)

val replyResponse1 = CommentResponse(
    id = 11L,
    body = "commenty comment",
    created_at = GregorianCalendar(1988, 1, 1).time,
    links = links
)

// constructed based on replyResponse1 data flattened, with replies
val replyWithReplies1 = CommentWithReplies(
    id = replyResponse1.id,
    parentId = replyResponse1.links.parentComment,
    body = replyResponse1.body,
    createdAt = replyResponse1.created_at,
    userId = replyResponse1.links.userId,
    storyId = replyResponse1.links.story,
    replies = emptyList()
)

// constructed based on replyWithReplies1 data flattened, with user data
val reply1 = Comment(
    id = replyResponse1.id,
    parentCommentId = parentId,
    body = replyResponse1.body,
    createdAt = replyResponse1.created_at,
    depth = replyResponse1.depth,
    upvotesCount = replyResponse1.vote_count,
    userId = replyResponse1.links.userId,
    userDisplayName = user1.displayName,
    userPortraitUrl = user1.portraitUrl,
    upvoted = false
)

val reply1NoUser = reply1.copy(userDisplayName = null, userPortraitUrl = null)

val replyResponse2 = CommentResponse(
    id = 12L,
    body = "commenty comment",
    created_at = GregorianCalendar(1908, 2, 8).time,
    links = links
)

// constructed based on replyResponse2 data flattened, with replies
val replyWithReplies2 = CommentWithReplies(
    id = replyResponse2.id,
    parentId = replyResponse2.links.parentComment,
    body = replyResponse2.body,
    createdAt = replyResponse2.created_at,
    userId = replyResponse2.links.userId,
    storyId = replyResponse2.links.story,
    replies = emptyList()
)

// constructed based on replyWithReplies2 data flattened, with user data
val reply2 = Comment(
    id = replyResponse2.id,
    parentCommentId = parentId,
    body = replyResponse2.body,
    createdAt = replyResponse2.created_at,
    depth = replyResponse2.depth,
    upvotesCount = replyResponse2.vote_count,
    userId = replyResponse2.links.userId,
    userDisplayName = user1.displayName,
    userPortraitUrl = user1.portraitUrl,
    upvoted = false
)

val repliesResponses = listOf(
    replyResponse1,
    replyResponse2
)

val parentLinks = CommentLinksResponse(
    userId = user2.id,
    story = 987L,
    parentComment = null,
    comments = arrayListOf(11L, 12L)
)

val parentCommentResponse = CommentResponse(
    id = parentId,
    body = "commenty comment",
    created_at = createdDate,
    links = parentLinks
)

// constructed based on parentCommentResponse data flattened, with replies
val parentCommentWithReplies = CommentWithReplies(
    id = parentCommentResponse.id,
    parentId = parentCommentResponse.links.parentComment,
    body = parentCommentResponse.body,
    createdAt = parentCommentResponse.created_at,
    userId = parentCommentResponse.links.userId,
    storyId = parentCommentResponse.links.story,
    replies = listOf(
        replyWithReplies1,
        replyWithReplies2
    )
)

val parentCommentWithRepliesWithoutReplies = parentCommentWithReplies.copy(replies = emptyList())

// constructed based on parentCommentWithReplies data flattened, with user
val parentComment = Comment(
    id = parentCommentResponse.id,
    parentCommentId = null,
    body = parentCommentResponse.body,
    createdAt = parentCommentResponse.created_at,
    depth = parentCommentResponse.depth,
    upvotesCount = parentCommentResponse.vote_count,
    userId = user2.id,
    userDisplayName = user2.displayName,
    userPortraitUrl = user2.portraitUrl,
    upvoted = false
)

val flattendCommentsWithReplies = listOf(parentComment, reply1, reply2)

val flattenedCommentsWithoutReplies = listOf(parentComment)

val errorResponseBody = ResponseBody.create(MediaType.parse(""), "Error")

val storyLinks = StoryLinks(
    user = 123L,
    comments = listOf(1, 2, 3),
    upvotes = listOf(11, 22, 33),
    downvotes = listOf(111, 222, 333)
)
