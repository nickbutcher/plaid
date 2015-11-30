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

/**
 * Models a comment on a designer news story.
 */
public class Comment implements Parcelable {

    public final long id;
    public final String body;
    public final String body_html;
    public final Date created_at;
    public final int depth;
    public int vote_count;
    public final long user_id;
    public final String user_display_name;
    public final String user_portrait_url;
    public final String user_job;
    public final List<Comment> comments;

    // TODO move this to a decorator
    public Boolean upvoted;

    public Comment(long id,
                   String body,
                   String body_html,
                   Date created_at,
                   int depth,
                   int vote_count,
                   long user_id,
                   String user_display_name,
                   String user_portrait_url,
                   String user_job,
                   List<Comment> comments) {
        this.id = id;
        this.body = body;
        this.body_html = body_html;
        this.created_at = created_at;
        this.depth = depth;
        this.vote_count = vote_count;
        this.user_id = user_id;
        this.user_display_name = user_display_name;
        this.user_portrait_url = user_portrait_url;
        this.user_job = user_job;
        this.comments = comments;
    }

    public static class Builder {
        private long id;
        private String body;
        private String body_html;
        private Date created_at;
        private int depth;
        private int vote_count;
        private long user_id;
        private String user_display_name;
        private String user_portrait_url;
        private String user_job;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setBodyHtml(String body_html) {
            this.body_html = body_html;
            return this;
        }

        public Builder setCreatedAt(Date created_at) {
            this.created_at = created_at;
            return this;
        }

        public Builder setDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder setVoteCount(int vote_count) {
            this.vote_count = vote_count;
            return this;
        }

        public Builder setUserId(long user_id) {
            this.user_id = user_id;
            return this;
        }

        public Builder setUserDisplayName(String user_display_name) {
            this.user_display_name = user_display_name;
            return this;
        }

        public Builder setUserPortraitUrl(String user_portrait_url) {
            this.user_portrait_url = user_portrait_url;
            return this;
        }

        public Builder setUserJob(String user_job) {
            this.user_job = user_job;
            return this;
        }

        public Comment build() {
            return new Comment(id, body, body_html, created_at, depth, vote_count, user_id,
                    user_display_name, user_portrait_url, user_job, null);
        }
    }

    /* parcelable */

    protected Comment(Parcel in) {
        id = in.readLong();
        body = in.readString();
        body_html = in.readString();
        long tmpCreated_at = in.readLong();
        created_at = tmpCreated_at != -1 ? new Date(tmpCreated_at) : null;
        depth = in.readInt();
        vote_count = in.readInt();
        user_id = in.readLong();
        user_display_name = in.readString();
        user_portrait_url = in.readString();
        user_job = in.readString();
        if (in.readByte() == 0x01) {
            comments = new ArrayList<Comment>();
            in.readList(comments, Comment.class.getClassLoader());
        } else {
            comments = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(body);
        dest.writeString(body_html);
        dest.writeLong(created_at != null ? created_at.getTime() : -1L);
        dest.writeInt(depth);
        dest.writeInt(vote_count);
        dest.writeLong(user_id);
        dest.writeString(user_display_name);
        dest.writeString(user_portrait_url);
        dest.writeString(user_job);
        if (comments == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(comments);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

}
