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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.customtabs.ICustomTabsService;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles the connection with the warmup service.
 *
 * The class instance must be accessed from one thread at a time.
 */
public class CustomTabActivityManager {
    private static final String TAG = "CustomTabsConnection";
    static final String STABLE_PACKAGE = "com.android.chrome";
    static final String BETA_PACKAGE = "com.chrome.beta";
    static final String DEV_PACKAGE = "com.chrome.dev";
    static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String EXTRA_CUSTOM_TABS_KEEP_ALIVE =
            "android.support.customtabs.extra.KEEP_ALIVE";

    private static final Object sConstructionLock = new Object();
    private static CustomTabActivityManager sInstance;

    private ICustomTabsService mService;
    private String mPackageNameToUse;
    private ServiceConnection mServiceConnection;

    private CustomTabActivityManager() {}

    /**
     * Get the instance.
     *
     * @return the instance of CustomTabActivityManager.
     */
    public static CustomTabActivityManager getInstance() {
        synchronized (sConstructionLock) {
            if (sInstance == null) {
                sInstance = new CustomTabActivityManager();
            }
            return sInstance;
        }
    }

    /**
     * Binds to the service.
     *
     * @return true for success.
     */
    public boolean bindService(Activity context, final ServiceConnectionCallback serviceConnectionCallback) {
        if (mService != null) return true;
        if (mPackageNameToUse == null) mPackageNameToUse = getPackageNameToUse(context);
        if (mPackageNameToUse == null) return false;
        Intent intent = CustomTabsSession.getServiceIntent(mPackageNameToUse, null);
        try {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mService = ICustomTabsService.Stub.asInterface(service);
                    if (serviceConnectionCallback != null) {
                        serviceConnectionCallback.onServiceConnected();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mService = null;
                }
            };
            return context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_WAIVE_PRIORITY);
        } catch (SecurityException e) {
            Log.e(TAG, "Error Binding", e);
            return false;
        }
    }

    public void unbindService(Activity activity) {
        if (mServiceConnection != null) {
            activity.unbindService(mServiceConnection);
            mServiceConnection = null;
            mService = null;
        }
    }

    public CustomTabsSession newSession(CustomTabsCallback callback) {
        if (mService == null) return null;
        return CustomTabsSession.newSession(mService, callback);
    }

    /**
     * Warms up the Browser.
     *
     * This require the service to be bound.
     *
     * @return true for success.
     */
    public boolean warmup() {
        Log.i(TAG, "service: " + mService );
        if (mService == null) return false;
        try {
            return mService.warmup(0L);
        } catch (RemoteException e) {
            Log.e(TAG, "Error warming up", e);
            return false;
        }
    }

    /**
     * Launches the CustomTabs activity with a given URL.
     *
     * @param context Activity context used to launch the CustomTabs activity.
     * @param session As returned by {@link CustomTabActivityManager#newSession(CustomTabsCallback)}
     * @param url URL to load in the CustomTabs activity
     * @param uiBuilder UI customizations
     */
    public void launchUrl(
            Activity context, CustomTabsSession session, String url, CustomTabUiBuilder uiBuilder) {
        if (mPackageNameToUse == null) mPackageNameToUse = getPackageNameToUse(context);
        Intent intent = CustomTabsIntent.getViewIntent(session, mPackageNameToUse, Uri.parse(url));
        intent.putExtras(uiBuilder.getExtraBundle());
        Intent keepAliveIntent = new Intent().setClassName(
                context.getPackageName(), KeepAliveService.class.getCanonicalName());
        intent.putExtra(EXTRA_CUSTOM_TABS_KEEP_ALIVE, keepAliveIntent);
        context.startActivity(intent, uiBuilder.getStartBundle());
    }

    /**
     * Goes through all apps that supports CATEGORY_CUSTOM_TABS in a service and VIEW intents. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     * @param context {@link Context} to use for accessing {@link PackageManager}.
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    public static String getPackageNameToUse(Context context) {
        PackageManager pm = context.getPackageManager();

        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        Set<String> resolvedActivityPackageList = new HashSet<>();
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent =
                    CustomTabsSession.getServiceIntent(info.activityInfo.packageName, null);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty()) return null;
        if (packagesSupportingCustomTabs.size() == 1) return packagesSupportingCustomTabs.get(0);
        if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            return defaultViewHandlerPackageName;
        }
        if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) return STABLE_PACKAGE;
        if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) return BETA_PACKAGE;
        if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) return DEV_PACKAGE;
        if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) return LOCAL_PACKAGE;
        return null;
    }

    /**
     * Used to check whether there is a specialized handler for a given intent.
     * @param intent The intent to check with.
     * @return Whether there is a specialized handler for the given intent.
     */
    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER);
            if (handlers == null || handlers.size() == 0) {
                return false;
            }
            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null) continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue;
                if (resolveInfo.activityInfo == null) continue;
                return true;
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Runtime exception while getting specialized handlers");
        }
        return false;
    }

    public interface ServiceConnectionCallback {
        void onServiceConnected();
    }
}
