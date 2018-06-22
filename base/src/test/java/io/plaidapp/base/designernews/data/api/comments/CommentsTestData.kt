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

package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.designernews.data.api.model.Comment
import io.plaidapp.base.designernews.data.api.model.CommentLinks
import okhttp3.MediaType
import okhttp3.ResponseBody

/**
 * Test data for comments
 */

val links = CommentLinks("userid", "storyid", 1L)

val childComment1 = Comment.Builder()
        .setId(11L)
        .setCommentLinks(links)
        .setBody("commenty comment")
        .build()

val childComment2 = Comment.Builder()
        .setId(12L)
        .setCommentLinks(links)
        .setBody("commenty comment")
        .build()

val childrenComments = listOf(childComment1, childComment2)

val parentLinks = CommentLinks("userid", "storyid", 1L, arrayListOf(11L, 12L))

val parentComment = Comment.Builder()
        .setId(1L)
        .setBody("commenty comment")
        .setCommentLinks(parentLinks)
        .setComments(childrenComments)
        .build()

val parentCommentWithoutChildren = Comment.Builder()
        .setId(1L)
        .setBody("commenty comment")
        .setCommentLinks(parentLinks)
        .build()

val errorResponseBody = ResponseBody.create(MediaType.parse(""), "Error")
