
package io.plaidapp.data.api.materialup.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Maker implements Parcelable {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("nickname")
    @Expose
    private String nickname;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("avatar_url")
    @Expose
    private String avatarUrl;

    private Maker(Builder builder) {
        setUrl(builder.url);
        setNickname(builder.nickname);
        setFullName(builder.fullName);
        setAvatarUrl(builder.avatarUrl);
    }

    /**
     * 
     * @return
     *     The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @param url
     *     The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * @return
     *     The nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 
     * @param nickname
     *     The nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 
     * @return
     *     The fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * 
     * @param fullName
     *     The full_name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * 
     * @return
     *     The avatarUrl
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 
     * @param avatarUrl
     *     The avatar_url
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.nickname);
        dest.writeString(this.fullName);
        dest.writeString(this.avatarUrl);
    }


    protected Maker(Parcel in) {
        this.url = in.readString();
        this.nickname = in.readString();
        this.fullName = in.readString();
        this.avatarUrl = in.readString();
    }

    public static final Creator<Maker> CREATOR = new Creator<Maker>() {
        @Override
        public Maker createFromParcel(Parcel source) {
            return new Maker(source);
        }

        @Override
        public Maker[] newArray(int size) {
            return new Maker[size];
        }
    };

    public static final class Builder {
        private String url;
        private String nickname;
        private String fullName;
        private String avatarUrl;

        public Builder() {
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder nickname(String val) {
            nickname = val;
            return this;
        }

        public Builder fullName(String val) {
            fullName = val;
            return this;
        }

        public Builder avatarUrl(String val) {
            avatarUrl = val;
            return this;
        }

        public Maker build() {
            return new Maker(this);
        }
    }
}
