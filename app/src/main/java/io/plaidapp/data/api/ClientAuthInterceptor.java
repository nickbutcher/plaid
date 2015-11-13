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

package io.plaidapp.data.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * A {@see RequestInterceptor} that adds an auth token to requests if one is provided, otherwise
 * adds a client id.
 */
public class ClientAuthInterceptor implements Interceptor {

    private String accessToken;
    private String clientId;
    private boolean hasAccessToken = false;

    public ClientAuthInterceptor(@Nullable String accessToken, @NonNull String clientId) {
        setAccessToken(accessToken);
        this.clientId = clientId;
    }

    private void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        hasAccessToken = !TextUtils.isEmpty(accessToken);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newRequest = null;
        if (hasAccessToken) {
            newRequest = request.newBuilder().addHeader("Authorization", "Bearer " + accessToken).build();
        } else {
            HttpUrl newHttpUrl = request.httpUrl().newBuilder().addQueryParameter("client_id", clientId).build();
            newRequest = request.newBuilder().url(newHttpUrl).build();
        }

        return chain.proceed(newRequest);
    }
}
