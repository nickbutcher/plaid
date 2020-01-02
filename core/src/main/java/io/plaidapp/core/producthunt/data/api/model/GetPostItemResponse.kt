/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.core.producthunt.data.api.model

import com.google.gson.annotations.SerializedName

/**
 * Models the get posts response for Product Hunt
 */
data class GetPostsResponse(
    @SerializedName("posts") val posts: List<GetPostItemResponse>
)

fun GetPostItemResponse.toPost() = Post(
    id = id,
    title = name,
    url = url,
    tagline = tagline,
    discussionUrl = discussionUrl,
    redirectUrl = redirectUrl,
    commentsCount = commentsCount,
    votesCount = votesCount
)
data class GetPostItemResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("url") var url: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("tagline") val tagline: String,
    @SerializedName("discussion_url") val discussionUrl: String,
    @SerializedName("redirect_url") val redirectUrl: String,
    @SerializedName("comments_count") val commentsCount: Int,
    @SerializedName("votes_count") val votesCount: Int
)
