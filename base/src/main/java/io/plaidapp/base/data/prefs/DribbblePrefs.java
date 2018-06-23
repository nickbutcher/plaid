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

package io.plaidapp.base.data.prefs;

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

import io.plaidapp.base.BuildConfig;
import io.plaidapp.base.data.api.AuthInterceptor;
import io.plaidapp.base.data.api.DenvelopingConverter;
import io.plaidapp.base.data.api.dribbble.DribbbleService;
import io.plaidapp.base.data.api.dribbble.model.User;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Storing dribbble user state.
 */
public class DribbblePrefs {

    private static volatile DribbblePrefs singleton;
    private DribbbleService api;

    public static DribbblePrefs get() {
        if (singleton == null) {
            synchronized (DribbblePrefs.class) {
                singleton = new DribbblePrefs();
            }
        }
        return singleton;
    }

    private DribbblePrefs() {
    }

    public DribbbleService getApi() {
        if (api == null) createApi();
        return api;
    }

    private void createApi() {
        final OkHttpClient client = new OkHttpClient.Builder().build();
        final Gson gson = new GsonBuilder()
                .setDateFormat(DribbbleService.DATE_FORMAT)
                .create();
        api = new Retrofit.Builder()
                .baseUrl(DribbbleService.ENDPOINT)
                .client(client)
                .addConverterFactory(new DenvelopingConverter(gson))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create((DribbbleService.class));
    }

}
