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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.net.Uri;
import android.support.customtabs.ICustomTabsCallback;
import android.support.customtabs.ICustomTabsService;


import java.util.List;

/**
 * Abstract service class for implementing Custom Tabs related functionality. The service should
 * be responding to the action ACTION_CUSTOM_TABS_CONNECTION. This class should be used by
 * implementers that want to provide Custom Tabs functionality, not by clients that want to launch
 * Custom Tabs.
 */
 public abstract class CustomTabsService extends Service {
     /**
      * The Intent action that a CustomTabsService must respond to.
      */
     public static final String ACTION_CUSTOM_TABS_CONNECTION =
             "android.support.customtabs.action.CustomTabsService";

     /**
     * For {@link CustomTabsService#mayLaunchUrl(ICustomTabsCallback, Uri, Bundle, List)} calls that
      * wants to specify more than one url, this key can be used with {@link Bundle#putString(String, String)}
     * to insert a new url to each bundle inside list of bundles.
      */
     public static final String KEY_URL =
             "android.support.customtabs.otherurls.URL";

     private ICustomTabsService.Stub mBinder = new ICustomTabsService.Stub() {

         @Override
         public boolean warmup(long flags) {
             return CustomTabsService.this.warmup(flags);
         }

         @Override
         public boolean newSession(ICustomTabsCallback callback) {
             return CustomTabsService.this.newSession(callback);
         }

         @Override
         public boolean mayLaunchUrl(ICustomTabsCallback callback, Uri url,
                         Bundle extras, List<Bundle> otherLikelyBundles) {
             return CustomTabsService.this.mayLaunchUrl(callback, url, extras, otherLikelyBundles);
         }
     };

     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }

     /**
      * Warms up the browser process asynchronously.
      *
      * @param flags Reserved for future use.
      * @return      Whether warm up was or had already been completed successfully. Multiple successful
      *              calls will return true.
      */
     public abstract boolean warmup(long flags);

     /**
      * Creates a new session through an ICustomTabsService with the optional callback. This session
      * can be used to associate any related communication through the service with an intent and
      * then later with a Custom Tab. The client can then send later service calls or intents to
      * through same session-intent-Custom Tab association.
      * @param callback Callback to be triggered on an external navigation. Can not be null.
      *                 Multiple calls with the same callback returns false, since a new session
      *                 object can not be created with the same {@link IBinder}.
      * @return         Whether a new session was successfully created.
      */
     public abstract boolean newSession(ICustomTabsCallback callback);

     /**
      * Tells the browser of a likely future navigation to a URL.
      *
      * The method {@link #warmup(long)} has to be called beforehand.
      * The most likely URL has to be specified explicitly. Optionally, a list of
      * other likely URLs can be provided. They are treated as less likely than
      * the first one, and have to be sorted in decreasing priority order. These
      * additional URLs may be ignored.
      * All previous calls to this method will be deprioritized.
      *
      * @param callback callback for navigation events
      * @param url Most likely URL.
      * @param extras Reserved for future use.
      * @param otherLikelyBundles Other likely destinations, sorted in decreasing
      *     likelihood order. Each Bundle has to provide a url.
      * @return Whether the call was successful.
      */
     public abstract boolean mayLaunchUrl(ICustomTabsCallback callback, Uri url,
             Bundle extras, List<Bundle> otherLikelyBundles);
}
