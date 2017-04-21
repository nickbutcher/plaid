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

package io.plaidapp.data.api.deviantart.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Models a Deviantart User
 */
public class User implements Parcelable{

//    {
//        userid (UUID)
//        username (string)
//        usericon (string)
//        type (string)
//        is_watching (boolean) Optional
//        details (object) Optional
//        {
//            sex (string|null)
//            age (integer|null)
//            joindate (string)
//        }
//        geo (object) Optional
//        {
//            country (string)
//            countryid (integer)
//            timezone (string)
//        }
//        profile (object) Optional
//        {
//            user_is_artist (boolean)
//            artist_level (string|null)
//            artist_speciality (string|null)
//            real_name (string)
//            tagline (string)
//            website (string)
//            cover_photo (string)
//            profile_pic (deviation object)
//        }
//        stats (object) Optional
//        {
//            watchers (integer)
//            friends (integer)
//        }
//    }
    public final UUID userid;
    public final String username;
    public final String usericon;
    public final String type;
    public final boolean is_watching;

    public final String portrait_url;
    public final String cover_photo_url;

    public User(UUID userid,
                String username,
                String usericon,
                String type,
                boolean is_watching,
                String portrait_url,
                String cover_photo_url) {
        this.userid = userid;
        this.username = username;
        this.usericon = usericon;
        this.type = type;
        this.is_watching = is_watching;
        this.portrait_url = portrait_url;
        this.cover_photo_url = cover_photo_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(userid);
        dest.writeString(username);
        dest.writeString(usericon);
        dest.writeString(type);
        dest.writeString(String.valueOf(is_watching));
    }

    public static class Builder {
        private UUID userid;
        private String username;
        private String usericon;
        private String type;
        private boolean is_watching;

        private String portraitUrl;
        private String coverPhotoUrl;

        public Builder setUserId(UUID userid) {
            this.userid = userid;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setUsericon(String usericon) {
            this.usericon = usericon;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setIsWatching(boolean is_watching) {
            this.is_watching = is_watching;
            return this;
        }

        public Builder setPortraitUrl(String portraitUrl) {
            this.portraitUrl = portraitUrl;
            return this;
        }

        public Builder setCoverPhotoUrl(String coverPhotoUrl) {
            this.coverPhotoUrl = coverPhotoUrl;
            return this;
        }

        public User build() {
            return new User(userid, username, usericon, type, is_watching, portraitUrl, coverPhotoUrl);
        }
    }

    protected User(Parcel in) {
        userid = (UUID) in.readValue(UUID.class.getClassLoader());
        username = in.readString();
        usericon = in.readString();
        type = in.readString();
        is_watching = Boolean.valueOf(in.readString());
        portrait_url = in.readString();
        cover_photo_url = in.readString();

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
