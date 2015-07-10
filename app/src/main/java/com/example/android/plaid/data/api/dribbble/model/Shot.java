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

package com.example.android.plaid.data.api.dribbble.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.android.plaid.data.PlaidItem;
import com.example.android.plaid.ui.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nickbutcher on 1/26/15.
 */
public class Shot extends PlaidItem implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Shot> CREATOR = new Parcelable.Creator<Shot>() {
        @Override
        public Shot createFromParcel(Parcel in) {
            return new Shot(in);
        }

        @Override
        public Shot[] newArray(int size) {
            return new Shot[size];
        }
    };
    public final String description;
    public final long width;
    public final long height;
    public final Images images;
    public final long views_count;
    public final long likes_count;
    public final long comments_count;
    public final long attachments_count;
    public final long rebounds_count;
    public final long buckets_count;
    public final Date created_at;
    public final Date updated_at;
    public final String html_url;
    public final String attachments_url;
    public final String buckets_url;
    public final String comments_url;
    public final String likes_url;
    public final String projects_url;
    public final String rebounds_url;
    public final List<String> tags;
    public final User user;
    public final Team team;
    // todo move this into a decorator
    public boolean hasFadedIn = false;
    public Spanned parsedDescription;

    public Shot(long id,
                String title,
                String description,
                long width,
                long height,
                Images images,
                long views_count,
                long likes_count,
                long comments_count,
                long attachments_count,
                long rebounds_count,
                long buckets_count,
                Date created_at,
                Date updated_at,
                String html_url,
                String attachments_url,
                String buckets_url,
                String comments_url,
                String likes_url,
                String projects_url,
                String rebounds_url,
                List<String> tags,
                User user,
                Team team) {
        super(id, title, html_url);
        this.description = description;
        this.width = width;
        this.height = height;
        this.images = images;
        this.views_count = views_count;
        this.likes_count = likes_count;
        this.comments_count = comments_count;
        this.attachments_count = attachments_count;
        this.rebounds_count = rebounds_count;
        this.buckets_count = buckets_count;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.html_url = html_url;
        this.attachments_url = attachments_url;
        this.buckets_url = buckets_url;
        this.comments_url = comments_url;
        this.likes_url = likes_url;
        this.projects_url = projects_url;
        this.rebounds_url = rebounds_url;
        this.tags = tags;
        this.user = user;
        this.team = team;
    }

    protected Shot(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        description = in.readString();
        width = in.readLong();
        height = in.readLong();
        images = (Images) in.readValue(Images.class.getClassLoader());
        views_count = in.readLong();
        likes_count = in.readLong();
        comments_count = in.readLong();
        attachments_count = in.readLong();
        rebounds_count = in.readLong();
        buckets_count = in.readLong();
        long tmpCreated_at = in.readLong();
        created_at = tmpCreated_at != -1 ? new Date(tmpCreated_at) : null;
        long tmpUpdated_at = in.readLong();
        updated_at = tmpUpdated_at != -1 ? new Date(tmpUpdated_at) : null;
        html_url = in.readString();
        url = html_url;
        attachments_url = in.readString();
        buckets_url = in.readString();
        comments_url = in.readString();
        likes_url = in.readString();
        projects_url = in.readString();
        rebounds_url = in.readString();
        tags = new ArrayList<String>();
        in.readStringList(tags);
        user = (User) in.readValue(User.class.getClassLoader());
        team = (Team) in.readValue(Team.class.getClassLoader());
        hasFadedIn = in.readByte() != 0x00;
    }

    public Spanned getParsedDescription(TextView textView) {
        if (parsedDescription == null && !TextUtils.isEmpty(description)) {
            parsedDescription = HtmlUtils.parseHtml(description, textView.getLinkTextColors(),
                    textView.getHighlightColor());
        }
        return parsedDescription;
    }

    public void setWeightRelativeToMax(long maxLikes) {
        weight = (float) likes_count / maxLikes * 0.8f;
        if (weightBoost > 0f) {
            weight += weightBoost;
            weight = Math.min(weight, 1f);
        }
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
        dest.writeString(description);
        dest.writeLong(width);
        dest.writeLong(height);
        dest.writeValue(images);
        dest.writeLong(views_count);
        dest.writeLong(likes_count);
        dest.writeLong(comments_count);
        dest.writeLong(attachments_count);
        dest.writeLong(rebounds_count);
        dest.writeLong(buckets_count);
        dest.writeLong(created_at != null ? created_at.getTime() : -1L);
        dest.writeLong(updated_at != null ? updated_at.getTime() : -1L);
        dest.writeString(html_url);
        dest.writeString(attachments_url);
        dest.writeString(buckets_url);
        dest.writeString(comments_url);
        dest.writeString(likes_url);
        dest.writeString(projects_url);
        dest.writeString(rebounds_url);
        dest.writeStringList(tags);
        dest.writeValue(user);
        dest.writeValue(team);
        dest.writeByte((byte) (hasFadedIn ? 0x01 : 0x00));
    }
}
