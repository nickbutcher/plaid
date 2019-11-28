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

package io.plaidapp.core.designernews.data.login.model

import com.google.gson.annotations.SerializedName

fun LoggedInUserResponse.toLoggedInUser(): LoggedInUser {
    return LoggedInUser(
        id = id,
        firstName = first_name,
        lastName = last_name,
        displayName = display_name,
        portraitUrl = portrait_url,
        upvotes = userLinks.upvotes
    )
}

/**
 * Models a Designer News logged in user response
 */
class LoggedInUserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("first_name") val first_name: String,
    @SerializedName("last_name") val last_name: String,
    @SerializedName("display_name") val display_name: String,
    @SerializedName("portrait_url") val portrait_url: String? = null,
    @SerializedName("links") val userLinks: UserLinks
)

class UserLinks(@SerializedName("comment_upvotes") val upvotes: List<Long>)
