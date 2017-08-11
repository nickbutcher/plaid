package io.plaidapp.data.api.designernews.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class StoryLinks implements Parcelable {

    public final String user;
    public final List<String> comments;
    public final List<String> upvotes;
    public final List<String> downvotes;

    public StoryLinks(
            String user,
            List<String> comments,
            List<String> upvotes,
            List<String> downvotes) {
        this.user = user;
        this.comments = comments;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.user);
        dest.writeStringList(this.comments);
        dest.writeStringList(this.upvotes);
        dest.writeStringList(this.downvotes);
    }

    protected StoryLinks(Parcel in) {
        this.user = in.readString();
        this.comments = in.createStringArrayList();
        this.upvotes = in.createStringArrayList();
        this.downvotes = in.createStringArrayList();
    }

    public static final Parcelable.Creator<StoryLinks> CREATOR = new Parcelable.Creator<StoryLinks>() {
        @Override
        public StoryLinks createFromParcel(Parcel source) {return new StoryLinks(source);}

        @Override
        public StoryLinks[] newArray(int size) {return new StoryLinks[size];}
    };
}
