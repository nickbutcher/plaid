/*
 * Copyright 2016 Google Inc.
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

package io.plaidapp.data.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import io.plaidapp.R;
import io.plaidapp.ui.PostNewDesignerNewsStory;

/**
 * Manager for working with the Post DN Story dynamic shortcut.
 */
public class PostShortcutManager {

    private static final String POST_SHORTCUT_ID = "post_dn_story";
    private static final List<String> SHORTCUT_IDS = Arrays.asList(POST_SHORTCUT_ID);

    private PostShortcutManager() { }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void enablePostShortcut(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return;
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

        Intent intent = new Intent(context, PostNewDesignerNewsStory.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ShortcutInfo postShortcut
                = new ShortcutInfo.Builder(context, POST_SHORTCUT_ID)
                .setShortLabel(context.getString(R.string.shortcut_post_short_label))
                .setLongLabel(context.getString(R.string.shortcut_post_long_label))
                .setDisabledMessage(context.getString(R.string.shortcut_post_disabled))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_post))
                .setIntent(intent)
                .build();
        shortcutManager.addDynamicShortcuts(Arrays.asList(postShortcut));
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void disablePostShortcut(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return;
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.disableShortcuts(SHORTCUT_IDS);
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static void reportUsed(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return;
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        shortcutManager.reportShortcutUsed(POST_SHORTCUT_ID);
    }

}
