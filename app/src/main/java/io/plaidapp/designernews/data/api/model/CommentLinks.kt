package io.plaidapp.designernews.data.api.model

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