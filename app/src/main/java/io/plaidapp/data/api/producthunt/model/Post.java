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

package io.plaidapp.data.api.producthunt.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.plaidapp.data.PlaidItem;
import io.plaidapp.util.ParcelUtils;

/**
 * Models a post on Product Hunt.
 */
public class Post extends PlaidItem implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
    public final String name;
    public final String tagline;
    public final String discussion_url;
    public final String redirect_url;
    //public final Date created_at;
    public final int comments_count;
    public final int votes_count;
    public final User user;
    public final List<User> makers;
    public final CurrentUser current_user;
    public final boolean maker_inside;
    public final Map<String, String> screenshot_url;

    public Post(long id,
                String name,
                String tagline,
                String discussion_url,
                String redirect_url,
                //Date created_at,
                int comments_count,
                int votes_count,
                User user,
                List<User> makers,
                CurrentUser current_user,
                boolean maker_inside,
                Map<String, String> screenshot_url) {
        super(id, name, discussion_url);
        this.name = name;
        //this.title = name;
        this.tagline = tagline;
        this.discussion_url = discussion_url;
        this.redirect_url = redirect_url;
        //this.created_at = created_at;
        this.comments_count = comments_count;
        this.votes_count = votes_count;
        this.user = user;
        this.makers = makers;
        this.current_user = current_user;
        this.maker_inside = maker_inside;
        this.screenshot_url = screenshot_url;
    }

    protected Post(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        name = in.readString();
        tagline = in.readString();
        discussion_url = in.readString();
        redirect_url = in.readString();
        long tmpCreated_at = in.readLong();
        //created_at = tmpCreated_at != -1 ? new Date(tmpCreated_at) : null;
        comments_count = in.readInt();
        votes_count = in.readInt();
        user = (User) in.readValue(User.class.getClassLoader());
        if (in.readByte() == 0x01) {
            makers = new ArrayList<User>();
            in.readList(makers, User.class.getClassLoader());
        } else {
            makers = null;
        }
        current_user = (CurrentUser) in.readValue(CurrentUser.class.getClassLoader());
        maker_inside = in.readByte() != 0x00;
        screenshot_url = ParcelUtils.readStringMap(in);
    }

    public String getScreenshotUrl(int width) {
        String url = null;
        for (String widthStr : screenshot_url.keySet()) {
            url = screenshot_url.get(widthStr);
            try {
                int screenshotWidth = Integer.parseInt(widthStr.substring(0, widthStr.length() -
                        2));
                if (screenshotWidth > width) {
                    break;
                }
            } catch (NumberFormatException nfe) {
            }
        }

        return url;
    }

    public void weigh(float maxProductHuntComments, float maxProductHuntVotes) {
        weight = 1f - ((((float) comments_count) / maxProductHuntComments) +
                ((float) votes_count / maxProductHuntVotes)) / 2f;
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
        dest.writeString(name);
        dest.writeString(tagline);
        dest.writeString(discussion_url);
        dest.writeString(redirect_url);
        //dest.writeLong(created_at != null ? created_at.getTime() : -1L);
        dest.writeInt(comments_count);
        dest.writeInt(votes_count);
        dest.writeValue(user);
        if (makers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(makers);
        }
        dest.writeValue(current_user);
        dest.writeByte((byte) (maker_inside ? 0x01 : 0x00));
        ParcelUtils.writeStringMap(screenshot_url, dest);
    }

    public static class CurrentUser implements Parcelable {

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<CurrentUser> CREATOR = new Parcelable
                .Creator<CurrentUser>() {
            @Override
            public CurrentUser createFromParcel(Parcel in) {
                return new CurrentUser(in);
            }

            @Override
            public CurrentUser[] newArray(int size) {
                return new CurrentUser[size];
            }
        };
        public final boolean voted_for_post;
        public final boolean commented_on_post;

        public CurrentUser(boolean voted_for_post,
                           boolean commented_on_post) {
            this.voted_for_post = voted_for_post;
            this.commented_on_post = commented_on_post;
        }

        protected CurrentUser(Parcel in) {
            voted_for_post = in.readByte() != 0x00;
            commented_on_post = in.readByte() != 0x00;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (voted_for_post ? 0x01 : 0x00));
            dest.writeByte((byte) (commented_on_post ? 0x01 : 0x00));
        }
    }
}
