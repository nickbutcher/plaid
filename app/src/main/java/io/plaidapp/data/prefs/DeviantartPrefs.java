package io.plaidapp.data.prefs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.plaidapp.BuildConfig;
import io.plaidapp.data.api.AuthInterceptor;
import io.plaidapp.data.api.DenvelopingConverter;
import io.plaidapp.data.api.deviantart.DeviantartService;
import io.plaidapp.data.api.deviantart.model.User;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

/**
 * Storing deviantart user state.
 */
public class DeviantartPrefs {

    public static final String LOGIN_CALLBACK = "deviantart-auth-callback";

//    public static final String LOGIN_URL = "https://dribbble.com/oauth/authorize?client_id="
//            + BuildConfig.DRIBBBLE_CLIENT_ID
//            + "&redirect_uri=plaid%3A%2F%2F" + LOGIN_CALLBACK
//            + "&scope=public+write+comment+upload";
//
    String LOGIN_URL = "https://www.deviantart.com/oauth2/authorize?client_id="
            + BuildConfig.DEVIANTART_CLIENT_ID
            + "&redirect_uri=foshizzle%3A%2F%2F" + LOGIN_CALLBACK
            + "&response_type=code&scope=basic%20browse";


    private static final String DEVIANTART_PREF = "DEVIANTART_PREF";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_USER_NAME = "KEY_USER_NAME";
    private static final String KEY_USER_USERNAME = "KEY_USER_USERNAME";
    private static final String KEY_USER_AVATAR = "KEY_USER_AVATAR";
    private static final String KEY_USER_TYPE = "KEY_USER_TYPE";
    private static final List<String> CREATIVE_TYPES
            = Arrays.asList(new String[] { "Player", "Team" });

    private static volatile DeviantartPrefs singleton;
    private final SharedPreferences prefs;

    private String accessToken;
    private boolean isLoggedIn = false;
    private UUID userId;
    private String userName;
    private String userUsername;
    private String userAvatar;
    private String userType;
    private DeviantartService api;
    private List<DeviantartLoginStatusListener> loginStatusListeners;

    public static DeviantartPrefs get(Context context) {
        if (singleton == null) {
            synchronized (DeviantartPrefs.class) {
                singleton = new DeviantartPrefs(context);
            }
        }
        return singleton;
    }

    private DeviantartPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(DEVIANTART_PREF, Context
                .MODE_PRIVATE);
        accessToken = prefs.getString(KEY_ACCESS_TOKEN, null);
        isLoggedIn = !TextUtils.isEmpty(accessToken);
        if (isLoggedIn) {
            userId = UUID.fromString(prefs.getString(KEY_USER_ID, null));
            userName = prefs.getString(KEY_USER_NAME, null);
            userUsername = prefs.getString(KEY_USER_USERNAME, null);
            userAvatar = prefs.getString(KEY_USER_AVATAR, null);
            userType = prefs.getString(KEY_USER_TYPE, null);
        }
    }

    public interface DeviantartLoginStatusListener {
        void onDeviantartLogin();
        void onDeviantartLogout();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setAccessToken(String accessToken) {
        if (!TextUtils.isEmpty(accessToken)) {
            this.accessToken = accessToken;
            isLoggedIn = true;
            prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
            createApi();
            dispatchLoginEvent();
        }
    }

    public void setLoggedInUser(User user) {
        if (user != null) {
            userName = user.username;
            userUsername = user.username;
            userId = user.userid;
            userAvatar = user.portrait_url;
            userType = user.type;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_ID, userId.toString());
            editor.putString(KEY_USER_NAME, userName);
            editor.putString(KEY_USER_USERNAME, userUsername);
            editor.putString(KEY_USER_AVATAR, userAvatar);
            editor.putString(KEY_USER_TYPE, userType);
            editor.apply();
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public boolean userCanPost() {
        return CREATIVE_TYPES.contains(userType);
    }

    public User getUser() {
        return new User.Builder()
                .setUserId(userId)
                .setUsername(userName)
                .setPortraitUrl(userAvatar)
                .setType(userType)
                .build();
    }

    public DeviantartService getApi() {
        if (api == null) createApi();
        return api;
    }

    public void logout() {
        isLoggedIn = false;
        accessToken = null;
        userId = null;
        userName = null;
        userUsername = null;
        userAvatar = null;
        userType = null;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, null);
        editor.putLong(KEY_USER_ID, 0l);
        editor.putString(KEY_USER_NAME, null);
        editor.putString(KEY_USER_AVATAR, null);
        editor.putString(KEY_USER_TYPE, null);
        editor.apply();
        createApi();
        dispatchLogoutEvent();
    }

    public void login(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LOGIN_URL)));
    }

    public void addLoginStatusListener(DeviantartLoginStatusListener listener) {
        if (loginStatusListeners == null) {
            loginStatusListeners = new ArrayList<>();
        }
        loginStatusListeners.add(listener);
    }

    public void removeLoginStatusListener(DeviantartLoginStatusListener listener) {
        if (loginStatusListeners != null) {
            loginStatusListeners.remove(listener);
        }
    }

    private void dispatchLoginEvent() {
        if (loginStatusListeners != null && !loginStatusListeners.isEmpty()) {
            for (DeviantartLoginStatusListener listener : loginStatusListeners) {
                listener.onDeviantartLogin();
            }
        }
    }

    private void dispatchLogoutEvent() {
        if (loginStatusListeners != null && !loginStatusListeners.isEmpty()) {
            for (DeviantartLoginStatusListener listener : loginStatusListeners) {
                listener.onDeviantartLogout();
            }
        }
    }

    private void createApi() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(getAccessToken()))
                .build();
        final Gson gson = new GsonBuilder()
                .setDateFormat(DeviantartService.DATE_FORMAT)
                .create();
        api = new Retrofit.Builder()
                .baseUrl(DeviantartService.ENDPOINT)
                .client(client)
                .addConverterFactory(new DenvelopingConverter(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create((DeviantartService.class));
    }

    public String getAccessToken() {
        return accessToken;
    }

  
}

