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

package io.plaidapp.core.designernews.data.login.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "logged_in_user")
data class LoggedInUser(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    val id: Long,

    @ColumnInfo(name = "first_name")
    @SerializedName("first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    @SerializedName("last_name")
    val lastName: String,

    @ColumnInfo(name = "display_name")
    @SerializedName("display_name")
    val displayName: String,

    @ColumnInfo(name = "potrait_url")
    @SerializedName("portrait_url")
    val portraitUrl: String? = null,

    @ColumnInfo(name = "upvotes")
    @SerializedName("upvotes")
    val upvotes: List<Long>
)
