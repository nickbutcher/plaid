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

import android.net.Uri;
import android.os.Bundle;

/**
 * A callback class for custom tabs client to get messages regarding events in their custom tabs.
 */
public class CustomTabsCallback {
    /**
     * To be called when a page navigation starts.
     *
     * @param url URL the user has navigated to.
     * @param extras Reserved for future use.
     */
    public void onUserNavigationStarted(Uri url, Bundle extras) {}

    /**
     * To be called when a page navigation finishes.
     *
     * @param url URL the user has navigated to.
     * @param extras Reserved for future use.
     */
    public void onUserNavigationFinished(Uri url, Bundle extras) {}
}
