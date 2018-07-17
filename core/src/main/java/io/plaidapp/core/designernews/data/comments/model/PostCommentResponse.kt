package io.plaidapp.core.designernews.data.comments.model

import com.google.gson.annotations.SerializedName

data class PostCommentResponse(@SerializedName("comments") val comments: List<CommentResponse>)
