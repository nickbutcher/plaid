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

package io.plaidapp.core.designernews.data.users.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "logged_in_user")
class LoggedInUser(
    id: Long,
    firstName: String,
    lastName: String,
    displayName: String,
    portraitUrl: String? = null,

    @ColumnInfo(name = "upvotes")
    @SerializedName("upvotes")
    val upvotes: List<Long>
) : User(id, firstName, lastName, displayName, portraitUrl) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoggedInUser

        if (upvotes != other.upvotes) return false

        return true
    }

    override fun hashCode(): Int {
        return upvotes.hashCode()
    }
}
