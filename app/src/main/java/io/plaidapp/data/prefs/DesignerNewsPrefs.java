/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
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
import io.plaidapp.data.api.designernews.login.DesignerNewsLoginLocalStorage;
import io.plaidapp.data.api.designernews.login.DesignerNewsLoginRepository;
import io.plaidapp.data.api.designernews.model.User;
import io.plaidapp.util.ShortcutHelper;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Storing Designer News user state
 */
public class DesignerNewsPrefs {

    private static volatile DesignerNewsPrefs singleton;

    private DesignerNewsService api;
    private DesignerNewsLoginRepository loginRepository;

    public static DesignerNewsPrefs get(Context context) {
        if (singleton == null) {
            synchronized (DesignerNewsPrefs.class) {
                singleton = new DesignerNewsPrefs(context);
            }
        }
        return singleton;
    }

    private DesignerNewsPrefs(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences
                (DesignerNewsLoginLocalStorage.DESIGNER_NEWS_PREF,
                        Context.MODE_PRIVATE);
        loginRepository = new DesignerNewsLoginRepository(new DesignerNewsLoginLocalStorage(prefs));
    }

    public boolean isLoggedIn() {
        return loginRepository.isLoggedIn();
    }

    public void setAccessToken(@NonNull Context context, String accessToken) {
        if (!TextUtils.isEmpty(accessToken)) {
            loginRepository.setAccessToken(accessToken);
            createApi(accessToken);
            ShortcutHelper.enablePostShortcut(context);
        }
    }

    public void setLoggedInUser(User user) {
        loginRepository.setLoggedInUser(user);
    }

    public User getUser() {
        return loginRepository.getUser();
    }

    public void logout(@NonNull Context context) {
        loginRepository.logout();
        createApi(loginRepository.getAccessToken());
        ShortcutHelper.disablePostShortcut(context);
    }

    public DesignerNewsService getApi() {
        if (api == null) createApi(loginRepository.getAccessToken());
        return api;
    }

    private void createApi(String accessToken) {
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(
                        new ClientAuthInterceptor(accessToken, BuildConfig.DESIGNER_NEWS_CLIENT_ID))
                .addInterceptor(getHttpLoggingInterceptor())
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

    @NonNull
    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor.Level
                debugLevel = BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE;
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(debugLevel);
        return loggingInterceptor;
    }

}
