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

import io.plaidapp.BuildConfig;
import io.plaidapp.data.api.MaterialUpAuthInterceptor;
import io.plaidapp.data.api.materialup.MaterialUpService;
import io.plaidapp.data.api.materialup.model.Maker;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Storing Material Up user state
 */
public class MaterialUpPrefs {

    private static final String MATERIAL_UP_PREF = "MATERIAL_UP_PREF";
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final String KEY_USER_NAME = "KEY_USER_NAME";
    private static final String KEY_USER_AVATAR = "KEY_USER_AVATAR";

    private static volatile MaterialUpPrefs singleton;
    private final SharedPreferences prefs;

    private String userId;
    private String username;
    private String userAvatar;
    private MaterialUpService api;

    public static MaterialUpPrefs get(Context context) {
        if (singleton == null) {
            synchronized (MaterialUpPrefs.class) {
                singleton = new MaterialUpPrefs(context);
            }
        }
        return singleton;
    }

    private MaterialUpPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(MATERIAL_UP_PREF, Context
                .MODE_PRIVATE);
        userId = prefs.getString(KEY_USER_ID, null);
        username = prefs.getString(KEY_USER_NAME, null);
        userAvatar = prefs.getString(KEY_USER_AVATAR, null);
    }



    public boolean isLoggedIn(){
        return  true;//// TODO: 28/09/16 materialUp api does not contain login,logout
    }

    public boolean userCanPost(){
        return true;//// TODO: 28/09/16 materialUp api does not contain login,logout
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public Maker getUser() {
        return new Maker.Builder()
                .nickname(userId)
                .fullName(username)
                .avatarUrl(userAvatar)
                .build();
    }


    public MaterialUpService getApi() {
        if (api == null) createApi();
        return api;
    }

    private void createApi() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new MaterialUpAuthInterceptor(BuildConfig.MATERIALUP_CLIENT_ACCESS_TOKEN))
                .build();
        api = new Retrofit.Builder()
                .baseUrl(MaterialUpService.ENDPOINT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MaterialUpService.class);
    }

}
