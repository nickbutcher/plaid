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

package io.plaidapp.core.designernews.data.api

import io.plaidapp.core.designernews.data.api.model.Comment
import io.plaidapp.core.designernews.data.api.model.CommentLinks
import okhttp3.MediaType
import okhttp3.ResponseBody

/**
 * Test data for comments
 */

const val parentId = 1L

val links = CommentLinks(userId = "userid", story = "storyid", parentComment = parentId)

val reply1 = Comment.Builder()
        .setId(11L)
        .setCommentLinks(links)
        .setBody("commenty comment")
        .build()

val reply2 = Comment.Builder()
        .setId(12L)
        .setCommentLinks(links)
        .setBody("commenty comment")
        .build()

val replies = listOf(reply1, reply2)

val parentLinks = CommentLinks(userId = "userid", story = "storyid", parentComment = 1L,
        comments = arrayListOf(11L, 12L))

val parentCommentWithReplies = Comment.Builder()
        .setId(parentId)
        .setBody("commenty comment")
        .setCommentLinks(parentLinks)
        .setReplies(replies)
        .build()

val parentCommentWithoutReplies = Comment.Builder()
        .setId(parentId)
        .setBody("commenty comment")
        .setCommentLinks(parentLinks)
        .build()

val errorResponseBody = ResponseBody.create(MediaType.parse(""), "Error")
