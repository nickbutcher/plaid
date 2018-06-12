package io.plaidapp.data.api.designernews.model

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