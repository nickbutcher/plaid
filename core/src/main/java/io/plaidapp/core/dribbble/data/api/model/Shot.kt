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

import android.content.res.ColorStateList
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.annotation.Keep
import android.text.Spanned
import com.google.gson.annotations.SerializedName
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.util.HtmlUtils
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * Models a dibbble shot
 */
@Keep
@Parcelize
data class Shot(
    @SerializedName("id") override val id: Long,
    @SerializedName("title") override val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null,
    @SerializedName("images") val images: Images,
    @SerializedName("views_count") val viewsCount: Long = 0L,
    @SerializedName("likes_count") val likesCount: Long = 0L,
    @SerializedName("created_at") val createdAt: Date? = null,
    @SerializedName("html_url") val htmlUrl: String = "https://dribbble.com/shots/$id",
    @SerializedName("animated") val animated: Boolean = false,
    @SerializedName("user") val user: User
) : PlaidItem(id, title, htmlUrl), Parcelable {

    // todo move this into a decorator
    @IgnoredOnParcel var hasFadedIn = false
    @IgnoredOnParcel var parsedDescription: Spanned? = null

    fun getParsedDescription(
        linkTextColor: ColorStateList,
        @ColorInt linkHighlightColor: Int
    ): Spanned? {
        if (parsedDescription == null && !description.isNullOrEmpty()) {
            parsedDescription = HtmlUtils.parseHtml(description, linkTextColor, linkHighlightColor)
        }
        return parsedDescription
    }
}
