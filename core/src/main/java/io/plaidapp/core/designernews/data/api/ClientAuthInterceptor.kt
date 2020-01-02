/*
 * Copyright 2019 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.core.designernews.data.api

import io.plaidapp.core.designernews.data.login.AuthTokenLocalDataSource
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An {@see Interceptor} that adds an auth token to requests if one is provided, otherwise
 * adds a client id.
 */
class ClientAuthInterceptor(
    private val authTokenDataSource: AuthTokenLocalDataSource,
    private val clientId: String
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        if (!authTokenDataSource.authToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization",
                    "Bearer ${authTokenDataSource.authToken}")
        } else {
            val url = chain.request().url.newBuilder()
                    .addQueryParameter("client_id", clientId).build()
            requestBuilder.url(url)
        }
        return chain.proceed(requestBuilder.build())
    }
}
