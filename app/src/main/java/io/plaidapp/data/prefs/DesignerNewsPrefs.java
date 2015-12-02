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

package io.plaidapp.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.data.api.designernews.model.User;

/**
 * Storing Designer News user state
 */
public class DesignerNewsPrefs {

    private static final String DESIGNER_NEWS_PREF = "DESIGNER_NEWS_PREF";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_USER_NAME = "KEY_USER_NAME";
    private static final String KEY_USER_AVATAR = "KEY_USER_AVATAR";

    private static volatile DesignerNewsPrefs singleton;
    private final SharedPreferences prefs;

    private String accessToken;
    private boolean isLoggedIn = false;
    private long userId;
    private String username;
    private String userAvatar;
    private List<DesignerNewsLoginStatusListener> loginStatusListeners;

    public static DesignerNewsPrefs get(Context context) {
        if (singleton == null) {
            synchronized (DesignerNewsPrefs.class) {
                singleton = new DesignerNewsPrefs(context);
            }
        }
        return singleton;
    }

    private DesignerNewsPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(DESIGNER_NEWS_PREF, Context
                .MODE_PRIVATE);
        accessToken = prefs.getString(KEY_ACCESS_TOKEN, null);
        isLoggedIn = !TextUtils.isEmpty(accessToken);
        if (isLoggedIn) {
            userId = prefs.getLong(KEY_USER_ID, 0l);
            username = prefs.getString(KEY_USER_NAME, null);
            userAvatar = prefs.getString(KEY_USER_AVATAR, null);
        }
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public @Nullable String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        if (!TextUtils.isEmpty(accessToken)) {
            this.accessToken = accessToken;
            isLoggedIn = true;
            prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
            dispatchLoginEvent();
        }
    }

    public void setLoggedInUser(User user) {
        if (user != null) {
            userId = user.id;
            username = user.display_name;
            userAvatar = user.portrait_url;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_USER_ID, userId);
            editor.putString(KEY_USER_NAME, username);
            editor.putString(KEY_USER_AVATAR, userAvatar);
            editor.apply();
        }
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public User getUser() {
        return new User.Builder()
                .setId(userId)
                .setDisplayName(username)
                .setPortraitUrl(userAvatar)
                .build();
    }

    public void logout() {
        isLoggedIn = false;
        accessToken = null;
        username = null;
        userAvatar = null;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, null);
        editor.putLong(KEY_USER_ID, 0l);
        editor.putString(KEY_USER_NAME, null);
        editor.putString(KEY_USER_AVATAR, null);
        editor.apply();
        dispatchLogoutEvent();
    }

    public void addLoginStatusListener(DesignerNewsLoginStatusListener listener) {
        if (loginStatusListeners == null) {
            loginStatusListeners = new ArrayList<>();
        }
        loginStatusListeners.add(listener);
    }

    public void removeLoginStatusListener(DesignerNewsLoginStatusListener listener) {
        if (loginStatusListeners != null) {
            loginStatusListeners.remove(listener);
        }
    }

    private void dispatchLoginEvent() {
        if (loginStatusListeners != null && loginStatusListeners.size() > 0) {
            for (DesignerNewsLoginStatusListener listener : loginStatusListeners) {
                listener.onDesignerNewsLogin();
            }
        }
    }

    private void dispatchLogoutEvent() {
        if (loginStatusListeners != null && loginStatusListeners.size() > 0) {
            for (DesignerNewsLoginStatusListener listener : loginStatusListeners) {
                listener.onDesignerNewsLogout();
            }
        }
    }

    public interface DesignerNewsLoginStatusListener {
        void onDesignerNewsLogin();
        void onDesignerNewsLogout();
    }

}
