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

package io.plaidapp.core.designernews.data.stories.model

import com.google.gson.annotations.SerializedName
import java.util.Date

fun getDefaultStoryUrl(id: Long) = "https://www.designernews.co/click/stories/$id"

fun StoryResponse.toStory() = Story(
    id = id,
    title = title,
    url = url,
    comment = comment,
    commentHtml = comment_html,
    commentCount = comment_count,
    voteCount = vote_count,
    createdAt = created_at,
    links = links
)

/**
 * Models a Designer News story response.
 */
data class StoryResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("url") var url: String? = getDefaultStoryUrl(id),
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("comment_html") val comment_html: String? = null,
    @SerializedName("comment_count") val comment_count: Int = 0,
    @SerializedName("vote_count") val vote_count: Int = 0,
    @SerializedName("created_at") val created_at: Date,
    @SerializedName("links") val links: StoryLinks? = null
)
