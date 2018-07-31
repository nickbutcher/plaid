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

package io.plaidapp.core.dribbble.data.api.model

import com.google.gson.annotations.SerializedName
import io.plaidapp.core.data.PlaidItem
import java.util.Date

/**
 * Models a dibbble shot
 */
data class Shot(
    @SerializedName("id") override val id: Long,
    @SerializedName("title") override val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("images") val images: Images,
    @SerializedName("views_count") val viewsCount: Int = 0,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("created_at") val createdAt: Date? = null,
    @SerializedName("html_url") val htmlUrl: String = "https://dribbble.com/shots/$id",
    @SerializedName("animated") val animated: Boolean = false,
    @SerializedName("user") val user: User
) : PlaidItem(id, title, htmlUrl) {

    // todo move this into a decorator
    var hasFadedIn = false
}
