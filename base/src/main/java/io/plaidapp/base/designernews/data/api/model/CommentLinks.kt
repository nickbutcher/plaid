/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.base.designernews.data.api.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Models story links received from DesignerNews v2 API
 */
data class CommentLinks(val user: String,
                        val story: String,
                        val comments: List<String>,
                        @SerializedName("comment_upvotes")
                        private val commentUpvotes: List<String>,
                        @SerializedName("comment_downvotes")
                        private val commentDownvotes: List<String>) : Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.createStringArrayList(),
            parcel.createStringArrayList(),
            parcel.createStringArrayList())

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user)
        parcel.writeString(story)
        parcel.writeStringList(comments)
        parcel.writeStringList(commentUpvotes)
        parcel.writeStringList(commentDownvotes)
    }

    companion object CREATOR : Parcelable.Creator<CommentLinks> {
        override fun createFromParcel(parcel: Parcel): CommentLinks {
            return CommentLinks(parcel)
        }

        override fun newArray(size: Int): Array<CommentLinks?> {
            return arrayOfNulls(size)
        }
    }
}
