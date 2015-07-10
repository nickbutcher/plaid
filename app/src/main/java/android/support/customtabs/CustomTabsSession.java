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
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.net.Uri;
import android.support.customtabs.ICustomTabsCallback;
import android.support.customtabs.ICustomTabsService;
import android.util.Log;

import java.util.List;

/**
 * A class to be used for Custom Tabs related communication. Clients that want to launch Custom Tabs
 * can use this class exclusively to handle all related communication.
 */
public class CustomTabsSession {
    private static final String TAG = "CustomTabsSession";
    private final ICustomTabsService mService;
    private final ICustomTabsCallback mCallback;

    /**
     * Convenience method to get an {@link Intent} for binding with a {@link ICustomTabsService}.
     * This sets the right action and makes the intent explicit to the provided package.
     * Client should be using {@link Context#BIND_AUTO_CREATE}
     * | {@link Context#BIND_WAIVE_PRIORITY} while calling
     * {@link Context#bindService(Intent, ServiceConnection, int)} with the given intent.
     * @param packageName The package name that the Intent will be set for.
     * @param connection The {@link ServiceConnection} to use while binding the service.
     * @return {@link Intent} to use for creating a binding with a {@link ICustomTabsService}.
     */
    public static Intent getServiceIntent(String packageName,
            ServiceConnection connection) {
        Intent intent = new Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
        intent.setPackage(packageName);
        return intent;
    }

    /**
     * Creates a new session through an ICustomTabsService with the optional callback. This session
     * can be used to associate any related communication through the service with an intent and
     * then later with a Custom Tab. The client can then send later service calls or intents to
     * through same session-intent-Custom Tab association.
     * @param service This is the {@link IBinder} returned through
     *                {@link ServiceConnection#onServiceConnected(android.content.ComponentName, IBinder)}
     *                when bound to any {@link CustomTabsService}. The returned session is only
     *                valid before {@link Service#unbindService(ServiceConnection)} or
     *                {@link ServiceConnection#onServiceDisconnected(android.content.ComponentName)}
     *                is called. Also see {@link DeathRecipient} for ways to monitor the lifecycle
     *                of the service.
     * @param callback The callback through which the client will receive updates about the created
     *                 session. Can be null.
     * @return The session object that was created as a result of the transaction. The client can
     *         use this to relay {@link CustomTabsSession#mayLaunchUrl(Uri, Bundle, List)} calls.
     */
    public static CustomTabsSession newSession(
            ICustomTabsService service, final CustomTabsCallback callback) {
        ICustomTabsCallback.Stub wrapper = new ICustomTabsCallback.Stub() {
            @Override
            public void onUserNavigationStarted(Uri url, Bundle extras) {
                if (callback != null) callback.onUserNavigationStarted(url, extras);
            }

            @Override
            public void onUserNavigationFinished(Uri url, Bundle extras) {
                if (callback != null) callback.onUserNavigationFinished(url, extras);
            }
        };

        try {
            if (!service.newSession(wrapper)) return null;
        } catch (RemoteException e) {
            return null;
        }
        return new CustomTabsSession(service, wrapper);
    }

    private CustomTabsSession(ICustomTabsService service, ICustomTabsCallback callback) {
        mService = service;
        mCallback = callback;
    }

    /**
     * Tells the browser of a likely future navigation to a URL.
     * The most likely URL has to be specified first. Optionally, a list of
     * other likely URLs can be provided. They are treated as less likely than
     * the first one, and have to be sorted in decreasing priority order. These
     * additional URLs may be ignored.
     * All previous calls to this method will be deprioritized.
     *
     * @param url                Most likely URL.
     * @param extras             Reserved for future use.
     * @param otherLikelyBundles Other likely destinations, sorted in decreasing
     *                           likelihood order. Each Bundle has to provide a url.
     * @return                   Whether the transaction was completed succesfully.
     */
    public boolean mayLaunchUrl(Uri url, Bundle extras, List<Bundle> otherLikelyBundles) {
        try {
            return mService.mayLaunchUrl(mCallback, url, extras, otherLikelyBundles);
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * @return A {@link Bundle} with the right EXTRA_SESSION related with
     *         this {@link CustomTabsSession}.
     */
    Bundle getBundleWithSession() {
        Bundle extras = new Bundle();
        extras.putBinder(CustomTabsIntent.EXTRA_SESSION, mCallback.asBinder());
        return extras;
    }
}