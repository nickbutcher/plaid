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

package io.plaidapp.data.pocket;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Adapted form https://github.com/Pocket/Pocket-AndroidWear-SDK/blob/master/library/src/com
 * /pocket/util/PocketUtil.java
 */
public class PocketUtils {

    private final static String PACKAGE = "com.ideashower.readitlater.pro";
    private final static String MIME_TYPE = "text/plain";
    private static final String EXTRA_SOURCE_PACKAGE = "source";
    private static final String EXTRA_TWEET_STATUS_ID = "tweetStatusId";

    public static void addToPocket(Context context,
                                   String url) {
        addToPocket(context, url, null);
    }

    public static void addToPocket(Context context,
                                   String url,
                                   String tweetStatusId) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(PACKAGE);
        intent.setType(MIME_TYPE);

        intent.putExtra(Intent.EXTRA_TEXT, url);
        if (tweetStatusId != null && tweetStatusId.length() > 0) {
            intent.putExtra(EXTRA_TWEET_STATUS_ID, tweetStatusId);
        }
        intent.putExtra(EXTRA_SOURCE_PACKAGE, context.getPackageName());
        context.startActivity(intent);
    }

    public static boolean isPocketInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info;
        try {
            info = pm.getPackageInfo(PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            info = null;
        }

        return info != null;
    }
}
