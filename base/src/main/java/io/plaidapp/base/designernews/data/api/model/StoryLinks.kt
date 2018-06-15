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

/**
 * Models story links received from DesignerNews v2 API
 */
data class StoryLinks(val user: String,
                      val comments: List<String>,
                      val upvotes: List<String>,
                      val downvotes: List<String>) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.createStringArrayList(),
            parcel.createStringArrayList(),
            parcel.createStringArrayList())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user)
        parcel.writeStringList(comments)
        parcel.writeStringList(upvotes)
        parcel.writeStringList(downvotes)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<StoryLinks> {
            override fun createFromParcel(parcel: Parcel): StoryLinks {
                return StoryLinks(parcel)
            }

            override fun newArray(size: Int): Array<StoryLinks?> {
                return arrayOfNulls(size)
            }
        }
    }
}