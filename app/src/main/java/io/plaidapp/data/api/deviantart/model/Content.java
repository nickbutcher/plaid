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
import android.text.TextUtils;

/**
 * Models links to the various quality of images of a shot.
 */
public class Content implements Parcelable {

//    private static final int[] NORMAL_IMAGE_SIZE = new int[] { 400, 300 };
//    private static final int[] TWO_X_IMAGE_SIZE = new int[] { 800, 600 };

    public final String src;
    public final int height;
    public final int width;
    public final boolean transparency;
    public final int filesize;

    public Content(String src, int height, int width, boolean transparency, int filesize) {
        this.src = src;
        this.height = height;
        this.width = width;
        this.transparency = transparency;
        this.filesize = filesize;
    }

    protected Content(Parcel in) {
        src = in.readString();
        height = in.readInt();
        width = in.readInt();
        transparency = Boolean.valueOf(in.readString());
        filesize = in.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(src);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeValue(transparency);
        dest.writeValue(filesize);

    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Content> CREATOR = new Parcelable.Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };

    public String getSource() {
        return src;
    }
}
