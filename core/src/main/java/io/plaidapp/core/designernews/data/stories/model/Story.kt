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

package io.plaidapp.core.designernews.data.stories.model

import com.google.gson.annotations.SerializedName
import io.plaidapp.core.data.PlaidItem
import java.util.Date

fun getDefaultUrl(id: Long) = "https://www.designernews.co/click/stories/$id"

/**
 * Models a Designer News story.
 * TODO split this into StoryRequest and Story, so we can keep the object immutable.
 */
data class Story(
    @SerializedName("id") override val id: Long,
    @SerializedName("title") override val title: String,
    @SerializedName("page") override val page: Int,
    @SerializedName("url")
    override var url: String? = getDefaultUrl(id),
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("comment_html") val commentHtml: String? = null,
    @SerializedName("comment_count") val commentCount: Int = 0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("links") val links: StoryLinks,
    @Deprecated("Removed in DN API V2")
    @SerializedName("user_display_name") val userDisplayName: String? = null,
    @Deprecated("Removed in DN API V2")
    @SerializedName("user_portrait_url") val userPortraitUrl: String? = null,
    @SerializedName("user_job") val userJob: String? = null
) : PlaidItem(id, title, url, page)
