/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.support.customtabs;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

/**
 * Constants and utilities that will be used for low level control on customizing the UI and
 * functionality of a tab.
 */
public class CustomTabsIntent {

    /**
     * Extra used to match the session. This has to be included in the intent to open in
     * a custom tab. This is the same IBinder that gets passed to ICustomTabsService#newSession.
     * Null if there is no need to match any service side sessions with the intent.
     */
    public static final String EXTRA_SESSION = "android.support.customtabs.extra.SESSION";

    /**
     * Extra that changes the background color for the toolbar. colorRes is an int that specifies a
     * {@link Color}, not a resource id.
     */
    public static final String EXTRA_TOOLBAR_COLOR =
            "android.support.customtabs.extra.TOOLBAR_COLOR";

    /**
     * Bundle used for adding a custom action button to the custom tab toolbar. The client can
     * provide an icon {@link Bitmap} and a {@link PendingIntent} for the button.
     */
    public static final String EXTRA_ACTION_BUTTON_BUNDLE =
            "android.support.customtabs.extra.ACTION_BUTTON_BUNDLE";

    /**
     * Key that specifies the {@link Bitmap} to be used as the image source for the action button.
     */
    public static final String KEY_ICON = "android.support.customtabs.customaction.ICON";

    /**
     * Key that specifies the PendingIntent to launch when the action button or menu item was
     * clicked. The custom tab will be calling {@link PendingIntent#send()} on clicks after adding
     * the url as data. The client app can call {@link Intent#getDataString()} to get the url.
     */
    public static final String KEY_PENDING_INTENT =
            "android.support.customtabs.customaction.PENDING_INTENT";

    /**
     * Use an {@code ArrayList<Bundle>} for specifying menu related params. There should be a
     * separate {@link Bundle} for each custom menu item.
     */
    public static final String EXTRA_MENU_ITEMS = "android.support.customtabs.extra.MENU_ITEMS";

    /**
     * Key for specifying the title of a menu item.
     */
    public static final String KEY_MENU_ITEM_TITLE =
            "android.support.customtabs.customaction.MENU_ITEM_TITLE";

    /**
     * Bundle constructed out of {@link ActivityOptions} that will be running when the
     * {@link Activity} that holds the custom tab gets finished. A similar ActivityOptions
     * for creation should be constructed and given to the startActivity() call that
     * launches the custom tab.
     */
    public static final String EXTRA_EXIT_ANIMATION_BUNDLE =
            "android.support.customtabs.extra.EXIT_ANIMATION_BUNDLE";

    /**
     * Convenience method to create a VIEW intent for a given session and package.
     * @param session     {@link CustomTabsSession} to associate the intent with. If this is null, the
     *                    intent returned won't be related with any session, but will still be
     *                    a valid Custom Tab intent.
     * @param packageName The package name to set in the intent.
     * @param data        The data {@link Uri} to be used in the intent.
     * @return            The intent with the given package, data and the right session extra.
     */
    public static Intent getViewIntent(CustomTabsSession session, String packageName, Uri data) {
        Intent intent = new Intent(Intent.ACTION_VIEW, data);
        intent.setPackage(packageName);
        Bundle extras;
        if (session == null) {
            extras = new Bundle();
            extras.putBinder(EXTRA_SESSION, null);
        } else {
            extras = session.getBundleWithSession();
        }
        intent.putExtras(extras);
        return intent;
    }
}
