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

package io.plaidapp.data.api.dribbble.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.Map;

import io.plaidapp.util.ParcelUtils;

/**
 * Models a dribbble user
 */
public class User implements Parcelable {

    public final long id;
    public final String name;
    public final String username;
    public final String html_url;
    public final String avatar_url;
    public final String bio;
    public final String location;
    public final Map<String, String> links;
    public final int buckets_count;
    public final int followers_count;
    public final int followings_count;
    public final int likes_count;
    public final int projects_count;
    public final int shots_count;
    public final int teams_count;
    public final String type;
    public final Boolean pro;
    public final String buckets_url;
    public final String followers_url;
    public final String following_url;
    public final String likes_url;
    public final String projects_url;
    public final String shots_url;
    public final String teams_url;
    public final Date created_at;
    public final Date updated_at;

    public User(long id,
                String name,
                String username,
                String html_url,
                String avatar_url,
                String bio,
                String location,
                Map<String, String> links,
                int buckets_count,
                int followers_count,
                int followings_count,
                int likes_count,
                int projects_count,
                int shots_count,
                int teams_count,
                String type,
                Boolean pro,
                String buckets_url,
                String followers_url,
                String following_url,
                String likes_url,
                String projects_url,
                String shots_url,
                String teams_url,
                Date created_at,
                Date updated_at) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.html_url = html_url;
        this.avatar_url = avatar_url;
        this.bio = bio;
        this.location = location;
        this.links = links;
        this.buckets_count = buckets_count;
        this.followers_count = followers_count;
        this.followings_count = followings_count;
        this.likes_count = likes_count;
        this.projects_count = projects_count;
        this.shots_count = shots_count;
        this.teams_count = teams_count;
        this.type = type;
        this.pro = pro;
        this.buckets_url = buckets_url;
        this.followers_url = followers_url;
        this.following_url = following_url;
        this.likes_url = likes_url;
        this.projects_url = projects_url;
        this.shots_url = shots_url;
        this.teams_url = teams_url;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    protected User(Parcel in) {
        id = in.readLong();
        name = in.readString();
        username = in.readString();
        html_url = in.readString();
        avatar_url = in.readString();
        bio = in.readString();
        location = in.readString();
        links = ParcelUtils.readStringMap(in);
        buckets_count = in.readInt();
        followers_count = in.readInt();
        followings_count = in.readInt();
        likes_count = in.readInt();
        projects_count = in.readInt();
        shots_count = in.readInt();
        teams_count = in.readInt();
        type = in.readString();
        byte proVal = in.readByte();
        pro = proVal == 0x02 ? null : proVal != 0x00;
        buckets_url = in.readString();
        followers_url = in.readString();
        following_url = in.readString();
        likes_url = in.readString();
        projects_url = in.readString();
        shots_url = in.readString();
        teams_url = in.readString();
        long tmpCreated_at = in.readLong();
        created_at = tmpCreated_at != -1 ? new Date(tmpCreated_at) : null;
        long tmpUpdated_at = in.readLong();
        updated_at = tmpUpdated_at != -1 ? new Date(tmpUpdated_at) : null;
    }

    public static class Builder {
        private long id;
        private String name;
        private String username;
        private String html_url = null;
        private String avatar_url;
        private String bio = null;
        private String location = null;
        private Map<String, String> links = null;
        private int buckets_count = 0;
        private int followers_count = 0;
        private int followings_count = 0;
        private int likes_count = 0;
        private int projects_count = 0;
        private int shots_count = 0;
        private int teams_count = 0;
        private String type = null;
        private Boolean pro = null;
        private String buckets_url = null;
        private String followers_url = null;
        private String following_url = null;
        private String likes_url = null;
        private String projects_url = null;
        private String shots_url = null;
        private String teams_url = null;
        private Date created_at = null;
        private Date updated_at = null;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setAvatarUrl(String avatar_url) {
            this.avatar_url = avatar_url;
            return this;
        }

        public Builder setHtmlUrl(String html_url) {
            this.html_url = html_url;
            return this;
        }

        public Builder setBio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder setLinks(Map<String, String> links) {
            this.links = links;
            return this;
        }

        public Builder setBucketsCount(int buckets_count) {
            this.buckets_count = buckets_count;
            return this;
        }

        public Builder setFollowersCount(int followers_count) {
            this.followers_count = followers_count;
            return this;
        }

        public Builder setFollowingsCount(int followings_count) {
            this.followings_count = followings_count;
            return this;
        }

        public Builder setLikesCount(int likes_count) {
            this.likes_count = likes_count;
            return this;
        }

        public Builder setProjectsCount(int projects_count) {
            this.projects_count = projects_count;
            return this;
        }

        public Builder setShotsCount(int shots_count) {
            this.shots_count = shots_count;
            return this;
        }

        public Builder setTeamsCount(int teams_count) {
            this.teams_count = teams_count;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setPro(Boolean pro) {
            this.pro = pro;
            return this;
        }

        public Builder setBucketsUrl(String buckets_url) {
            this.buckets_url = buckets_url;
            return this;
        }

        public Builder setFollowersUrl(String followers_url) {
            this.followers_url = followers_url;
            return this;
        }

        public Builder setFollowingUrl(String following_url) {
            this.following_url = following_url;
            return this;
        }

        public Builder setLikesUrl(String likes_url) {
            this.likes_url = likes_url;
            return this;
        }

        public Builder setProjectsUrl(String projects_url) {
            this.projects_url = projects_url;
            return this;
        }

        public Builder setShotsUrl(String shots_url) {
            this.shots_url = shots_url;
            return this;
        }

        public Builder setTeamsUrl(String teams_url) {
            this.teams_url = teams_url;
            return this;
        }

        public Builder setCreatedAt(Date created_at) {
            this.created_at = created_at;
            return this;
        }

        public Builder setUpdatedAt(Date updated_at) {
            this.updated_at = updated_at;
            return this;
        }

        public User build() {
            return new User(id,
                    name,
                    username,
                    html_url,
                    avatar_url,
                    bio,
                    location,
                    links,
                    buckets_count,
                    followers_count,
                    followings_count,
                    likes_count,
                    projects_count,
                    shots_count,
                    teams_count,
                    type,
                    pro,
                    buckets_url,
                    followers_url,
                    following_url,
                    likes_url,
                    projects_url,
                    shots_url,
                    teams_url,
                    created_at,
                    updated_at);
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
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(html_url);
        dest.writeString(avatar_url);
        dest.writeString(bio);
        dest.writeString(location);
        ParcelUtils.writeStringMap(links, dest);
        dest.writeInt(buckets_count);
        dest.writeInt(followers_count);
        dest.writeInt(followings_count);
        dest.writeInt(likes_count);
        dest.writeInt(projects_count);
        dest.writeInt(shots_count);
        dest.writeInt(teams_count);
        dest.writeString(type);
        if (pro == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (pro ? 0x01 : 0x00));
        }
        dest.writeString(buckets_url);
        dest.writeString(followers_url);
        dest.writeString(following_url);
        dest.writeString(likes_url);
        dest.writeString(projects_url);
        dest.writeString(shots_url);
        dest.writeString(teams_url);
        dest.writeLong(created_at != null ? created_at.getTime() : -1L);
        dest.writeLong(updated_at != null ? updated_at.getTime() : -1L);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
