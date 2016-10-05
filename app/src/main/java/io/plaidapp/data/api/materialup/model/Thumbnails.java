
package io.plaidapp.data.api.materialup.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Thumbnails implements Parcelable {

    @SerializedName("teaser_url")
    @Expose
    private String teaserUrl;
    @SerializedName("preview_url")
    @Expose
    private String previewUrl;

    /**
     * 
     * @return
     *     The teaserUrl
     */
    public String getTeaserUrl() {
        return teaserUrl;
    }

    /**
     * 
     * @param teaserUrl
     *     The teaser_url
     */
    public void setTeaserUrl(String teaserUrl) {
        this.teaserUrl = teaserUrl;
    }

    /**
     * 
     * @return
     *     The previewUrl
     */
    public String getPreviewUrl() {
        return previewUrl;
    }

    /**
     * 
     * @param previewUrl
     *     The preview_url
     */
    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.teaserUrl);
        dest.writeString(this.previewUrl);
    }

    public Thumbnails() {
    }

    protected Thumbnails(Parcel in) {
        this.teaserUrl = in.readString();
        this.previewUrl = in.readString();
    }

    public static final Parcelable.Creator<Thumbnails> CREATOR = new Parcelable.Creator<Thumbnails>() {
        @Override
        public Thumbnails createFromParcel(Parcel source) {
            return new Thumbnails(source);
        }

        @Override
        public Thumbnails[] newArray(int size) {
            return new Thumbnails[size];
        }
    };
}
