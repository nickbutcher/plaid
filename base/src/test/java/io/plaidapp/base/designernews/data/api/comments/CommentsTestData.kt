package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.designernews.data.api.model.Comment
import io.plaidapp.base.designernews.data.api.model.CommentLinks
import okhttp3.MediaType
import okhttp3.ResponseBody

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
