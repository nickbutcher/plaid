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

package io.plaidapp.core.producthunt.data.api.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.plaidapp.core.data.PlaidItem
import kotlinx.android.parcel.Parcelize

/**
 * Models a post on Product Hunt.
 */
@Parcelize
class Post(
    @SerializedName("id") override val id: Long,
    @SerializedName("title") override val title: String,
    @SerializedName("url") override var url: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("tagline") val tagline: String,
    @SerializedName("discussion_url") val discussionUrl: String,
    @SerializedName("redirect_url") val redirectUrl: String,
    @SerializedName("comments_count") val commentsCount: Int,
    @SerializedName("votes_count") val votesCount: Int
) : PlaidItem(id, title, url), Parcelable
