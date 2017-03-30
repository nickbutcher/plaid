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
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;

import io.plaidapp.BuildConfig;
import io.plaidapp.data.api.ClientAuthInterceptor;
import io.plaidapp.data.api.DenvelopingConverter;
import io.plaidapp.data.api.designernews.DesignerNewsService;
import io.plaidapp.data.api.designernews.model.User;
import io.plaidapp.util.ShortcutHelper;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private DesignerNewsService api;

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

    public void setAccessToken(@NonNull Context context, String accessToken) {
        if (!TextUtils.isEmpty(accessToken)) {
            this.accessToken = accessToken;
            isLoggedIn = true;
            prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
            createApi();
            ShortcutHelper.enablePostShortcut(context);
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

    public void logout(@NonNull Context context) {
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
        createApi();
        ShortcutHelper.disablePostShortcut(context);
    }

    public DesignerNewsService getApi() {
        if (api == null) createApi();
        return api;
    }

    private void createApi() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(
                        new ClientAuthInterceptor(accessToken, BuildConfig.DESIGNER_NEWS_CLIENT_ID))
                .build();
        final Gson gson = new Gson();
        api = new Retrofit.Builder()
                .baseUrl(DesignerNewsService.ENDPOINT)
                .client(client)
                .addConverterFactory(new DenvelopingConverter(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(DesignerNewsService.class);
    }

}
