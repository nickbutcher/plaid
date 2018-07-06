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

package io.plaidapp.core.designernews.data.api.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * Models a comment on a designer news story.
 */
@Parcelize
data class Comment(
    val id: Long,
    val parentComment: Long?,
    val body: String,
    val created_at: Date,
    val depth: Int,
    val voteCount: Int,
    val replies: List<Comment>,
    val userId: Long?,
    val userDisplayName: String,
    val userPortraitUrl: String?,
    var upvoted: Boolean
) : Parcelable