/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.data.api.designernews.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.plaidapp.data.PlaidItem;

/**
 * Models a Designer News story
 */
public class Story extends PlaidItem implements Parcelable {

    public final String comment;
    public final String comment_html;
    public final int comment_count;
    public final int vote_count;
    public final Date created_at;
    public final long user_id;
    public final String user_display_name;
    public final String user_portrait_url;
    public final String hostname;
    public final String badge;
    public final String user_job;
    public final List<Comment> comments;

    public Story(long id,
                 String title,
                 String url,
                 String comment,
                 String comment_html,
                 int comment_count,
                 int vote_count,
                 Date created_at,
                 long user_id,
                 String user_display_name,
                 String user_portrait_url,
                 String hostname,
                 String badge,
                 String user_job,
                 List<Comment> comments) {
        super(id, title, url);
        this.comment = comment;
        this.comment_html = comment_html;
        this.comment_count = comment_count;
        this.vote_count = vote_count;
        this.created_at = created_at;
        this.user_id = user_id;
        this.user_display_name = user_display_name;
        this.user_portrait_url = user_portrait_url;
        this.hostname = hostname;
        this.badge = badge;
        this.user_job = user_job;
        this.comments = comments;
    }

    protected Story(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        comment = in.readString();
        comment_html = in.readString();
        comment_count = in.readInt();
        vote_count = in.readInt();
        long tmpCreated_at = in.readLong();
        created_at = tmpCreated_at != -1 ? new Date(tmpCreated_at) : null;
        user_id = in.readLong();
        user_display_name = in.readString();
        user_portrait_url = in.readString();
        hostname = in.readString();
        badge = in.readString();
        user_job = in.readString();
        if (in.readByte() == 0x01) {
            comments = new ArrayList<Comment>();
            in.readList(comments, Comment.class.getClassLoader());
        } else {
            comments = null;
        }
    }

    public void weigh(float maxDesignNewsComments, float maxDesignNewsVotes) {
        weight = 1f - ((((float) comment_count) / maxDesignNewsComments) +
                ((float) vote_count / maxDesignNewsVotes)) / 2f;
        weight = Math.min(weight + weightBoost, 1f);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(comment);
        dest.writeString(comment_html);
        dest.writeInt(comment_count);
        dest.writeInt(vote_count);
        dest.writeLong(created_at != null ? created_at.getTime() : -1L);
        dest.writeLong(user_id);
        dest.writeString(user_display_name);
        dest.writeString(user_portrait_url);
        dest.writeString(hostname);
        dest.writeString(badge);
        dest.writeString(user_job);
        if (comments == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(comments);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Story> CREATOR = new Parcelable.Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }
    };

}
