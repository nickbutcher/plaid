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

package com.example.android.plaid.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.android.plaid.data.api.dribbble.model.User;

/**
 * Created by nickbutcher on 2/7/15.
 */
public class DesignerNewsPrefs {

    // STOPSHIP stop 'borrowing' this client_id – https://github
    // .com/MengTo/DesignerNewsApp/blob/master/DesignerNewsApp/DesignerNewsService.swift
    public static final String CLIENT_ID =
            "750ab22aac78be1c6d4bbe584f0e3477064f646720f327c5464bc127100a1a6d";
    public static final String CLIENT_SECRET =
            "53e3822c49287190768e009a8f8e55d09041c5bf26d0ef982693f215c72d87da";
    private static final String DESIGNER_NEWS_PREF = "DESIGNER_NEWS_PREF";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_USER_NAME = "KEY_USER_NAME";
    private static final String KEY_USER_AVATAR = "KEY_USER_AVATAR";
    private final SharedPreferences prefs;

    private String accessToken;
    private boolean isLoggedIn = false;
    private String username;
    private String userAvatar;

    public DesignerNewsPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(DESIGNER_NEWS_PREF, Context
                .MODE_PRIVATE);
        accessToken = prefs.getString(KEY_ACCESS_TOKEN, null);
        isLoggedIn = !TextUtils.isEmpty(accessToken);
        if (isLoggedIn) {
            username = prefs.getString(KEY_USER_NAME, null);
            userAvatar = prefs.getString(KEY_USER_AVATAR, null);
        }
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getAccessToken() {
        return !TextUtils.isEmpty(accessToken) ? accessToken : CLIENT_ID;
    }

    public void setAccessToken(String accessToken) {
        if (!TextUtils.isEmpty(accessToken)) {
            this.accessToken = accessToken;
            isLoggedIn = true;
            prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
        }
    }

    public void setLoggedInUser(User user) {
        if (user != null) {
            username = user.username;
            userAvatar = user.avatar_url;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_NAME, username);
            editor.putString(KEY_USER_AVATAR, userAvatar);
            editor.apply();
        }
    }

    public String getUserName() {
        return username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void logout() {
        isLoggedIn = false;
        accessToken = null;
        username = null;
        userAvatar = null;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, null);
        editor.putString(KEY_USER_NAME, null);
        editor.putString(KEY_USER_AVATAR, null);
        editor.apply();
    }
}
