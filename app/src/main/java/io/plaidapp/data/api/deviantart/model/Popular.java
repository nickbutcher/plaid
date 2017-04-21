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

import java.util.List;

public class Popular implements Parcelable {

    private final boolean has_more;
    public final int next_offset;
    public final long estimated_total;
    public final List<Deviation> results;

    public Popular(boolean has_more, int next_offset, long estimated_total, List<Deviation> results) {
        this.has_more = has_more;
        this.next_offset = next_offset;
        this.estimated_total = estimated_total;
        this.results = results;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(has_more));
        dest.writeInt(next_offset);
        dest.writeLong(estimated_total);
        dest.writeValue(results);
    }

    public static class Builder {
        private boolean has_more;
        public int next_offset;
        public long estimated_total;
        public  List<Deviation> results;

        private Builder setHasMore(boolean has_more){
            this.has_more = has_more;
            return this;
        }

        private Builder setNextOffset(int next_offset){
            this.next_offset = next_offset;
            return this;
        }

        private Builder setEstimatedTotal(long estimated_total){
            this.estimated_total = estimated_total;
            return this;
        }

        private Builder setResults(List<Deviation> results){
            this.results = results;
            return this;
        }

        public Popular build() {
            return new Popular(has_more, next_offset, estimated_total, results);
        }

    }

    protected Popular(Parcel in){
        has_more = Boolean.valueOf(in.readString());
        next_offset = in.readInt();
        estimated_total = in.readLong();
        results = (List<Deviation>)in.readValue(Object.class.getClassLoader());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Popular> CREATOR = new Parcelable.Creator<Popular>() {
        @Override
        public Popular createFromParcel(Parcel in) {
            return new Popular(in);
        }

        @Override
        public Popular[] newArray(int size) {
            return new Popular[size];
        }
    };
}
