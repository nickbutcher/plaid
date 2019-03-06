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

package io.plaidapp.designernews.dagger

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import io.plaidapp.core.BuildConfig
import io.plaidapp.core.dagger.CoreDataModule
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.core.data.api.DeEnvelopingConverter
import io.plaidapp.core.designernews.data.login.AuthTokenLocalDataSource
import io.plaidapp.designernews.data.api.ClientAuthInterceptor
import io.plaidapp.designernews.data.api.DesignerNewsService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Retention(BINARY)
@Qualifier
private annotation class LocalApi

/**
 * Dagger module to provide data functionality for DesignerNews.
 */
@Module(
    includes = [
        SharedPreferencesModule::class,
        CoreDataModule::class
    ]
)
class DataModule {

    @LocalApi
    @Provides
    fun providePrivateOkHttpClient(
        upstream: OkHttpClient,
        tokenHolder: AuthTokenLocalDataSource
    ): OkHttpClient {
        return upstream.newBuilder()
            .addInterceptor(ClientAuthInterceptor(tokenHolder, BuildConfig.DESIGNER_NEWS_CLIENT_ID))
            .build()
    }

    @Provides
    fun provideDesignerNewsService(
        @LocalApi client: Lazy<OkHttpClient>,
        gson: Gson
    ): DesignerNewsService {
        return Retrofit.Builder()
            .baseUrl(DesignerNewsService.ENDPOINT)
            .callFactory { client.get().newCall(it) }
            .addConverterFactory(DeEnvelopingConverter(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(DesignerNewsService::class.java)
    }
}
