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

package io.plaidapp.core.producthunt.data.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.plaidapp.core.data.PlaidItem;

/**
 * Models a post on Product Hunt.
 */
public class Post extends PlaidItem implements Parcelable {

    public final String name;
    public final String tagline;
    public final String discussion_url;
    public final String redirect_url;
    public final int comments_count;
    public final int votes_count;
    public final boolean maker_inside;

    public Post(long id,
            String name,
            String tagline,
            String discussion_url,
            String redirect_url,
            int comments_count,
            int votes_count,
            boolean maker_inside) {
        super(id, name, discussion_url);
        this.name = name;
        this.tagline = tagline;
        this.discussion_url = discussion_url;
        this.redirect_url = redirect_url;
        this.comments_count = comments_count;
        this.votes_count = votes_count;
        this.maker_inside = maker_inside;
    }

    protected Post(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        name = in.readString();
        tagline = in.readString();
        discussion_url = in.readString();
        redirect_url = in.readString();
        comments_count = in.readInt();
        votes_count = in.readInt();
        maker_inside = in.readByte() != 0x00;
    }

    /* Parcelable stuff */

    @SuppressWarnings("unused")
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(name);
        dest.writeString(tagline);
        dest.writeString(discussion_url);
        dest.writeString(redirect_url);
        dest.writeInt(comments_count);
        dest.writeInt(votes_count);
        dest.writeByte((byte) (maker_inside ? 0x01 : 0x00));
    }

}
