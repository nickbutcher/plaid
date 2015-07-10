// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.chromium.customtabsclient;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;

import java.util.ArrayList;

/**
 * Helper class to build the Custom Tab Activity UI.
 */
public class CustomTabUiBuilder {
    // Flags that are not included in android support library yet.
    public static final String EXTRA_TITLE_VISIBILITY_STATE =
            "android.support.customtabs.extra.TITLE_VISIBILITY";
    private static final int NO_TITLE = 0;
    private static final int SHOW_PAGE_TITLE = 1;

    public static final String EXTRA_CLOSE_BUTTON_STYLE =
            "android.support.customtabs.extra.CLOSE_BUTTON_STYLE";
    public static final int CLOSE_BUTTON_CROSS = 0;
    public static final int CLOSE_BUTTON_ARROW = 1;

    private final Bundle mExtras = new Bundle();
    private Bundle mStartBundle = null;
    private final ArrayList<Bundle> mMenuItems = new ArrayList<>();

    public CustomTabUiBuilder() {}

    /**
     * Sets the toolbar color.
     *
     * @param color The color.
     */
    public CustomTabUiBuilder setToolbarColor(@ColorInt int color) {
        mExtras.putInt(CustomTabsIntent.EXTRA_TOOLBAR_COLOR, color);
        return this;
    }

    /**
     * Sets the toolbar color by resource id
     *
     * @param colorRes The resource id of the color
     */
    public CustomTabUiBuilder setToolbarColorRes(@NonNull Context context, @ColorRes int colorRes) {
        if (context != null) {
            mExtras.putInt(CustomTabsIntent.EXTRA_TOOLBAR_COLOR, context.getColor(colorRes));
        }
        return this;
    }

    /**
     * Sets whether the title should be shown in the custom tab.
     *
     * @param showTitle Whether the title should be shown.
     */
    public void setShowTitle(boolean showTitle) {
        int titleVisibilityState = showTitle ? SHOW_PAGE_TITLE : NO_TITLE;
        mExtras.putInt(EXTRA_TITLE_VISIBILITY_STATE, titleVisibilityState);
    }

    /**
     * Sets the style of the close button on toolbar.
     *
     * @param style Either {@link #CLOSE_BUTTON_CROSS} or {@link #CLOSE_BUTTON_ARROW}
     */
    public void setCloseButtonStyle(int style) {
        if (style == CLOSE_BUTTON_CROSS || style == CLOSE_BUTTON_ARROW) {
            mExtras.putInt(EXTRA_CLOSE_BUTTON_STYLE, style);
        }
    }

    /**
     * Adds a menu item.
     *
     * @param label Menu label.
     * @param pendingIntent Pending intent delivered when the menu item is clicked.
     */
    public CustomTabUiBuilder addMenuItem(String label, PendingIntent pendingIntent) {
        Bundle bundle = new Bundle();
        bundle.putString(CustomTabsIntent.KEY_MENU_ITEM_TITLE, label);
        bundle.putParcelable(CustomTabsIntent.KEY_PENDING_INTENT, pendingIntent);
        mMenuItems.add(bundle);
        return this;
    }

    /**
     * Set the action button.
     *
     * @param bitmap The icon.
     * @param pendingIntent pending intent delivered when the button is clicked.
     */
    public CustomTabUiBuilder setActionButton(Bitmap bitmap, PendingIntent pendingIntent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CustomTabsIntent.KEY_ICON, bitmap);
        bundle.putParcelable(CustomTabsIntent.KEY_PENDING_INTENT, pendingIntent);
        mExtras.putBundle(CustomTabsIntent.EXTRA_ACTION_BUTTON_BUNDLE, bundle);
        return this;
    }

    /**
     * Sets the start animations,
     *
     * @param context Application context.
     * @param enterResId Resource ID of the "enter" animation for the browser.
     * @param exitResId Resource ID of the "exit" animation for the application.
     */
    public CustomTabUiBuilder setStartAnimations(Context context, @AnimRes int enterResId, @AnimRes int exitResId) {
        mStartBundle =
                ActivityOptions.makeCustomAnimation(context, enterResId, exitResId).toBundle();
        return this;
    }

    /**
     * Sets the exit animations,
     *
     * @param context Application context.
     * @param enterResId Resource ID of the "enter" animation for the application.
     * @param exitResId Resource ID of the "exit" animation for the browser.
     */
    public CustomTabUiBuilder setExitAnimations(Context context, @AnimRes int enterResId, @AnimRes int exitResId) {
        Bundle bundle =
                ActivityOptions.makeCustomAnimation(context, enterResId, exitResId).toBundle();
        mExtras.putBundle(CustomTabsIntent.EXTRA_EXIT_ANIMATION_BUNDLE, bundle);
        return this;
    }

    Bundle getExtraBundle() {
        if (!mMenuItems.isEmpty()) {
            mExtras.putParcelableArrayList(CustomTabsIntent.EXTRA_MENU_ITEMS, mMenuItems);
        }
        return mExtras;
    }

    Bundle getStartBundle() {
        return mStartBundle;
    }
}
