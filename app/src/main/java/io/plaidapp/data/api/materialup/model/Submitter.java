
package io.plaidapp.data.api.materialup.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Submitter implements Parcelable {

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

    public Submitter() {
    }

    protected Submitter(Parcel in) {
        this.url = in.readString();
        this.nickname = in.readString();
        this.fullName = in.readString();
        this.avatarUrl = in.readString();
    }

    public static final Parcelable.Creator<Submitter> CREATOR = new Parcelable.Creator<Submitter>() {
        @Override
        public Submitter createFromParcel(Parcel source) {
            return new Submitter(source);
        }

        @Override
        public Submitter[] newArray(int size) {
            return new Submitter[size];
        }
    };
}
