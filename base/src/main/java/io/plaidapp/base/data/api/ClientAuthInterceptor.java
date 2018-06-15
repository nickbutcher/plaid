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

package io.plaidapp.base.data.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;

import io.plaidapp.designernews.data.api.DesignerNewsAuthTokenHolder;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An {@see Interceptor} that adds an auth token to requests if one is provided, otherwise
 * adds a client id.
 */
public class ClientAuthInterceptor implements Interceptor {

    private final DesignerNewsAuthTokenHolder tokenHolder;
    private final String clientId;

    public ClientAuthInterceptor(@Nullable DesignerNewsAuthTokenHolder tokenHolder,
            @NonNull String clientId) {
        this.tokenHolder = tokenHolder;
        this.clientId = clientId;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request.Builder requestBuilder = chain.request().newBuilder();
        if (!TextUtils.isEmpty(tokenHolder.getAuthToken())) {
            requestBuilder.addHeader("Authorization", "Bearer " + tokenHolder.getAuthToken());
        } else {
            final HttpUrl url = chain.request().url().newBuilder()
                    .addQueryParameter("client_id", clientId).build();
            requestBuilder.url(url);
        }
        return chain.proceed(requestBuilder.build());
    }
}
