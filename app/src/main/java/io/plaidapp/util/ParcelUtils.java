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

package io.plaidapp.util;

import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for working with Parcels.
 */
public class ParcelUtils {

    private ParcelUtils() { }

    public static void writeStringMap(Map<String, String> map, Parcel parcel) {
        if (map != null && map.size() > 0) {
            parcel.writeInt(map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                parcel.writeString(entry.getKey());
                parcel.writeString(entry.getValue());
            }
        } else {
            parcel.writeInt(0);
        }
    }

    public static Map<String, String> readStringMap(Parcel parcel) {
        Map<String, String> map = null;
        int size = parcel.readInt();
        if (size > 0) {
            map = new HashMap<String, String>(size);
            for (int i = 0; i < size; i++) {
                String key = parcel.readString();
                String value = parcel.readString();
                map.put(key, value);
            }
        }
        return map;
    }
}
