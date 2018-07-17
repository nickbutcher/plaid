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

package io.plaidapp.core.designernews.data.comments.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Models comment links received from DesignerNews v2 API
 */
@Parcelize
data class CommentLinksResponse(
    @SerializedName("user") val userId: Long,
    @SerializedName("story") val story: Long,
    @SerializedName("parent_comment") val parentComment: Long? = null,
    @SerializedName("comments") val comments: List<Long> = emptyList(),
    @SerializedName("comment_upvotes") val commentUpvotes: List<String> = emptyList(),
    @SerializedName("comment_downvotes") val commentDownvotes: List<String> = emptyList()
) : Parcelable
