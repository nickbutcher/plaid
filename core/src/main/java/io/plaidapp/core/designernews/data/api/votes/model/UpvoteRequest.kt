package io.plaidapp.core.designernews.data.api.votes.model

import com.google.gson.annotations.SerializedName

class UpvoteRequest(
        storyId: Long,
        userId: Long,
        @SerializedName("upvotes") val upvote: Upvote = Upvote(VoteLinks(storyId, userId))
)

data class Upvote(@SerializedName("links") val voteLinks: VoteLinks)

data class VoteLinks(@SerializedName("story") val storyId: Long,
                     @SerializedName("user") val userId: Long)