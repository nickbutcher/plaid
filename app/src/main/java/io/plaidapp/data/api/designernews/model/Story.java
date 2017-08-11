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

import java.util.Date;

import io.plaidapp.data.PlaidItem;

/**
 * Models a Designer News story
 */
public class Story extends PlaidItem implements Parcelable {

    public final String comment;
    public final String comment_html;
    public final int comment_count;
    public final int vote_count;
    public final long user_id;
    public final Date created_at;
    public final StoryLinks links;
    public final String user_display_name;   // Gone
    public final String user_portrait_url;   // Gone
    public final String hostname;
    public final String badge;
    public final String user_job;   // Gone

    public Story(
            long id,
            String title,
            String url,
            String comment,
            String comment_html,
            int comment_count,
            int vote_count,
            long user_id,
            Date created_at,
            String user_display_name,
            String user_portrait_url,
            String hostname,
            String badge,
            String user_job,
            StoryLinks links) {
        super(id, title, url);
        this.comment = comment;
        this.comment_html = comment_html;
        this.comment_count = comment_count;
        this.vote_count = vote_count;
        this.user_id = user_id;
        this.created_at = created_at;
        this.links = links;
        this.user_display_name = user_display_name;
        this.user_portrait_url = user_portrait_url;
        this.hostname = hostname;
        this.badge = badge;
        this.user_job = user_job;
    }

    public static class Builder {

        private long id;
        private String title;
        private String url;
        private String comment;
        private String commentHtml;
        private int commentCount;
        private int voteCount;
        private long user_id;
        private Date createdAt;
        private StoryLinks links;
        private String userDisplayName;
        private String userPortraitUrl;
        private String hostname;
        private String badge;
        private String userJob;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setDefaultUrl(long id) {
            this.url = "https://www.designernews.co/click/stories/" + id;
            return this;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder setCommentHtml(String comment_html) {
            this.commentHtml = comment_html;
            return this;
        }

        public Builder setCommentCount(int comment_count) {
            this.commentCount = comment_count;
            return this;
        }

        public Builder setVoteCount(int vote_count) {
            this.voteCount = vote_count;
            return this;
        }

        public Builder setCreatedAt(Date created_at) {
            this.createdAt = created_at;
            return this;
        }

        public Builder setLinks(StoryLinks links) {
            this.links = links;
            return this;
        }

        public Builder setUserId(long user_id) {
            this.user_id = user_id;
            return this;
        }

        public Builder setUserDisplayName(String user_display_name) {
            this.userDisplayName = user_display_name;
            return this;
        }

        public Builder setUserPortraitUrl(String user_portrait_url) {
            this.userPortraitUrl = user_portrait_url;
            return this;
        }

        public Builder setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder setBadge(String badge) {
            this.badge = badge;
            return this;
        }

        public Builder setUserJob(String user_job) {
            this.userJob = user_job;
            return this;
        }

        public Story build() {
            return new Story(id, title, url, comment, commentHtml, commentCount, voteCount,
                    user_id, createdAt, userDisplayName, userPortraitUrl, hostname, badge,
                    userJob, links);
        }

        public static Builder from(Story existing) {
            return new Builder()
                    .setId(existing.id)
                    .setTitle(existing.title)
                    .setUrl(existing.url)
                    .setComment(existing.comment)
                    .setCommentHtml(existing.comment_html)
                    .setCommentCount(existing.comment_count)
                    .setVoteCount(existing.vote_count)
                    .setCreatedAt(existing.created_at)
                    .setLinks(existing.links)
                    .setUserDisplayName(existing.user_display_name)
                    .setUserPortraitUrl(existing.user_portrait_url)
                    .setHostname(existing.hostname)
                    .setBadge(existing.badge)
                    .setUserJob(existing.user_job);
        }
    }

    /* Parcelable stuff */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(dataSource);
        dest.writeString(this.comment);
        dest.writeString(this.comment_html);
        dest.writeInt(this.comment_count);
        dest.writeInt(this.vote_count);
        dest.writeLong(this.user_id);
        dest.writeLong(this.created_at != null ? this.created_at.getTime() : -1);
        dest.writeParcelable(this.links, flags);
        dest.writeString(this.user_display_name);
        dest.writeString(this.user_portrait_url);
        dest.writeString(this.hostname);
        dest.writeString(this.badge);
        dest.writeString(this.user_job);
    }

    protected Story(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        this.dataSource = in.readString();
        this.comment = in.readString();
        this.comment_html = in.readString();
        this.comment_count = in.readInt();
        this.vote_count = in.readInt();
        this.user_id = in.readLong();
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
        this.links = in.readParcelable(StoryLinks.class.getClassLoader());
        this.user_display_name = in.readString();
        this.user_portrait_url = in.readString();
        this.hostname = in.readString();
        this.badge = in.readString();
        this.user_job = in.readString();
    }

    public static final Creator<Story> CREATOR = new Creator<Story>() {
        @Override
        public Story createFromParcel(Parcel source) {return new Story(source);}

        @Override
        public Story[] newArray(int size) {return new Story[size];}
    };
}
