
package io.plaidapp.data.api.materialup.model;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.text.Spanned;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.data.PlaidItem;
import io.plaidapp.util.DribbbleUtils;

public class Post extends PlaidItem implements Parcelable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("slug")
    @Expose
    private String slug;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("redirect_url")
    @Expose
    public String redirectUrl;
    @SerializedName("thumbnails")
    @Expose
    private Thumbnails thumbnails;
    @SerializedName("upvotes_count")
    @Expose
    private int upvotesCount;
    @SerializedName("collections_count")
    @Expose
    private int collectionsCount;
    @SerializedName("points")
    @Expose
    private int points;
    @SerializedName("comments_count")
    @Expose
    private int commentsCount;
    @SerializedName("view_count")
    @Expose
    private int viewCount;
    @SerializedName("platform")
    @Expose
    private String platform;
    @SerializedName("source")
    @Expose
    private Source source;
    @SerializedName("published_at")
    @Expose
    private String publishedAt;
    @SerializedName("submitter")
    @Expose
    private Submitter submitter;
    @SerializedName("makers")
    @Expose
    private List<Maker> makers = new ArrayList<>();
    @SerializedName("category")
    @Expose
    private Category category;

    public boolean hasFadedIn = false;

    public Spanned parsedDescription;


    public Post(long id,String name,String slug,
                String label,String redirectUrl,Thumbnails thumbnails,
                int upvotesCount,int collectionsCount,int points,
                int commentsCount,int viewCount,String platform,Source source,String publishedAt,Submitter submitter,List<Maker> makers,Category category){
        super(id, name, "dddiscussion_url");
        this.name = name;
        this.slug = slug;
        this.label = label;
        this.redirectUrl = redirectUrl;
        this.thumbnails = thumbnails;
        this.upvotesCount = upvotesCount;
        this.collectionsCount = collectionsCount;
        this.points= points;
        this.commentsCount = commentsCount;
        this.viewCount = viewCount;
        this.platform = platform;
        this.source = source;
        this.publishedAt = publishedAt;
        this.submitter = submitter;
        this.makers = makers;
        this.category = category;
    }
    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * 
     * @param slug
     *     The slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * 
     * @return
     *     The label
     */
    public String getLabel() {
        return label;
    }

    /**
     * 
     * @param label
     *     The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 
     * @return
     *     The redirectUrl
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * 
     * @param redirectUrl
     *     The redirect_url
     */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * 
     * @return
     *     The thumbnails
     */
    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    /**
     * 
     * @param thumbnails
     *     The thumbnails
     */
    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    /**
     * 
     * @return
     *     The upvotesCount
     */
    public int getUpvotesCount() {
        return upvotesCount;
    }

    /**
     * 
     * @param upvotesCount
     *     The upvotes_count
     */
    public void setUpvotesCount(int upvotesCount) {
        this.upvotesCount = upvotesCount;
    }

    /**
     * 
     * @return
     *     The collectionsCount
     */
    public int getCollectionsCount() {
        return collectionsCount;
    }

    /**
     * 
     * @param collectionsCount
     *     The collections_count
     */
    public void setCollectionsCount(int collectionsCount) {
        this.collectionsCount = collectionsCount;
    }

    /**
     * 
     * @return
     *     The points
     */
    public int getPoints() {
        return points;
    }

    /**
     * 
     * @param points
     *     The points
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * 
     * @return
     *     The commentsCount
     */
    public int getCommentsCount() {
        return commentsCount;
    }

    /**
     * 
     * @param commentsCount
     *     The comments_count
     */
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    /**
     * 
     * @return
     *     The viewCount
     */
    public int getViewCount() {
        return viewCount;
    }

    /**
     * 
     * @param viewCount
     *     The view_count
     */
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * 
     * @return
     *     The platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 
     * @param platform
     *     The platform
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * 
     * @return
     *     The source
     */
    public Source getSource() {
        return source;
    }

    /**
     * 
     * @param source
     *     The source
     */
    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * 
     * @return
     *     The publishedAt
     */
    public String getPublishedAt() {
        return publishedAt;
    }

    /**
     * 
     * @param publishedAt
     *     The published_at
     */
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    /**
     * 
     * @return
     *     The submitter
     */
    public Submitter getSubmitter() {
        return submitter;
    }

    /**
     * 
     * @param submitter
     *     The submitter
     */
    public void setSubmitter(Submitter submitter) {
        this.submitter = submitter;
    }

    /**
     * 
     * @return
     *     The makers
     */
    public List<Maker> getMakers() {
        return makers;
    }

    /**
     * 
     * @param makers
     *     The makers
     */
    public void setMakers(List<Maker> makers) {
        this.makers = makers;
    }

    /**
     * 
     * @return
     *     The category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * 
     * @param category
     *     The category
     */
    public void setCategory(Category category) {
        this.category = category;
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
        dest.writeString(this.name);
        dest.writeString(this.slug);
        dest.writeString(this.label);
        dest.writeString(this.redirectUrl);
        dest.writeParcelable(this.thumbnails, flags);
        dest.writeInt(this.upvotesCount);
        dest.writeInt(this.collectionsCount);
        dest.writeInt(this.points);
        dest.writeInt(this.commentsCount);
        dest.writeInt(this.viewCount);
        dest.writeString(this.platform);
        dest.writeParcelable(this.source, flags);
        dest.writeString(this.publishedAt);
        dest.writeParcelable(this.submitter, flags);
        dest.writeTypedList(this.makers);
        dest.writeParcelable(this.category, flags);
    }

    protected Post(Parcel in) {
        super(in.readLong(), in.readString(), in.readString());
        this.name = in.readString();
        this.slug = in.readString();
        this.label = in.readString();
        this.redirectUrl = in.readString();
        this.thumbnails = in.readParcelable(Thumbnails.class.getClassLoader());
        this.upvotesCount = in.readInt();
        this.collectionsCount = in.readInt();
        this.points = in.readInt();
        this.commentsCount = in.readInt();
        this.viewCount = in.readInt();
        this.platform = in.readString();
        this.source = in.readParcelable(Source.class.getClassLoader());
        this.publishedAt = in.readString();
        this.submitter = in.readParcelable(Submitter.class.getClassLoader());
        this.makers = in.createTypedArrayList(Maker.CREATOR);
        this.category = in.readParcelable(Category.class.getClassLoader());
    }

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public Spanned getParsedDescription(ColorStateList linkTextColor,
                                        @ColorInt int linkHighlightColor) {
        if (parsedDescription == null && !TextUtils.isEmpty(name)) {
            parsedDescription = DribbbleUtils.parseDribbbleHtml(name, linkTextColor,
                    linkHighlightColor);
        }
        return parsedDescription;
    }
}
