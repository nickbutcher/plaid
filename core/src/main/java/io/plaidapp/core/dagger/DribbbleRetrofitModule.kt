/*
 * Copyright 2018 Google, Inc.
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

package io.plaidapp.core.dagger

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import io.plaidapp.core.dribbble.data.search.DribbbleSearchConverter
import io.plaidapp.core.dribbble.data.search.DribbbleSearchService
import io.plaidapp.core.loggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Module to set up retrofit.
 */
@Module
class DribbbleRetrofitModule {

    @Provides fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    @Provides fun provideRetrofitInterface(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(DribbbleSearchService.ENDPOINT)
            .addConverterFactory(DribbbleSearchConverter.Factory())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
}
