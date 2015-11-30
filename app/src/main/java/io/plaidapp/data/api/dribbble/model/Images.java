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

package io.plaidapp.data.api.dribbble.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Models links to the various quality of images of a shot.
 */
public class Images implements Parcelable {

    private static final int[] NORMAL_IMAGE_SIZE = new int[] { 400, 300 };
    private static final int[] TWO_X_IMAGE_SIZE = new int[] { 800, 600 };

    public final String hidpi;
    public final String normal;
    public final String teaser;

    public Images(String hidpi, String normal, String teaser) {
        this.hidpi = hidpi;
        this.normal = normal;
        this.teaser = teaser;
    }

    protected Images(Parcel in) {
        hidpi = in.readString();
        normal = in.readString();
        teaser = in.readString();
    }

    public String best() {
        return !TextUtils.isEmpty(hidpi) ? hidpi : normal;
    }

    public int[] bestSize() {
        return !TextUtils.isEmpty(hidpi) ? TWO_X_IMAGE_SIZE : NORMAL_IMAGE_SIZE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hidpi);
        dest.writeString(normal);
        dest.writeString(teaser);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Images> CREATOR = new Parcelable.Creator<Images>() {
        @Override
        public Images createFromParcel(Parcel in) {
            return new Images(in);
        }

        @Override
        public Images[] newArray(int size) {
            return new Images[size];
        }
    };

}
