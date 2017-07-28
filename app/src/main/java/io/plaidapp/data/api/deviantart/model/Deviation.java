package io.plaidapp.data.api.deviantart.model;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.text.Spanned;
import android.text.TextUtils;


import io.plaidapp.data.PlaidItem;
import io.plaidapp.util.DribbbleUtils;

/**
 * Created by macbook on 02/04/17.
 */


public class Deviation extends PlaidItem implements Parcelable {

    public final String description;
    public final Content content;
//    public final Stats stats;

    // todo move this into a decorator
    public boolean hasFadedIn = false;
    public Spanned parsedDescription;


    public Deviation(long id, String title, String description, String html_url, Content content){//, Stats stats){
        super(id, title, html_url);
        this.description = description;
        this.content = content;
//        this.stats = stats;
    }

    public Deviation(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        description = in.readString();
        content = (Content) in.readValue(Content.class.getClassLoader());
//        stats = (Stats) in.readValue(Stats.class.getClassLoader());
        hasFadedIn = in.readByte() != 0x00;
    }

    public static class Builder {
        private long id;
        private String title;
        private String html_url;
        private String description;
        private Content content;
        private Stats stats;

        public Deviation.Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Deviation.Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setHtmlUrl(String html_url) {
            this.html_url = html_url;
            return this;
        }

        public Deviation.Builder setDescription(String description) {
            this.description = description;
            return this;
        }


        public Builder setContent(Content content) {
            this.content = content;
            return this;
        }

        public Builder setStats(Stats stats){
            this.stats = stats;
            return this;
        }

        public Deviation build() {
            return new Deviation(id, title, html_url, description, content);//, stats);
        }

    }

     /* Parcelable stuff */

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Deviation> CREATOR = new Parcelable.Creator<Deviation>() {
        @Override
        public Deviation createFromParcel(Parcel in) {
            return new Deviation(in);
        }

        @Override
        public Deviation[] newArray(int size) {
            return new Deviation[size];
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
        dest.writeString(description);
        dest.writeValue(content);
//        dest.writeValue(stats);
        dest.writeByte((byte) (hasFadedIn ? 0x01 : 0x00));

    }

    public Spanned getParsedDescription(ColorStateList linkTextColor,
                                        @ColorInt int linkHighlightColor) {
        if (parsedDescription == null && !TextUtils.isEmpty(description)) {
            parsedDescription = DribbbleUtils.parseDribbbleHtml(description, linkTextColor,
                    linkHighlightColor);
        }
        return parsedDescription;
    }
}
