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

@file:JvmName("Injection")

package io.plaidapp.core.designernews

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import io.plaidapp.core.BuildConfig
import io.plaidapp.core.data.api.DenvelopingConverter
import io.plaidapp.core.designernews.data.api.ClientAuthInterceptor
import io.plaidapp.core.designernews.data.api.DesignerNewsAuthTokenLocalDataSource
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginLocalDataSource
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginRemoteDataSource
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginRepository
import io.plaidapp.core.loggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * File providing different dependencies.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */

fun provideDesignerNewsLoginLocalDataSource(context: Context): DesignerNewsLoginLocalDataSource {
    val preferences = provideSharedPreferences(
            context,
            DesignerNewsLoginLocalDataSource.DESIGNER_NEWS_PREF)
    return DesignerNewsLoginLocalDataSource(preferences)
}

private fun provideSharedPreferences(context: Context, name: String): SharedPreferences {
    return context.applicationContext
            .getSharedPreferences(name, Context.MODE_PRIVATE)
}

fun provideDesignerNewsLoginRepository(context: Context): DesignerNewsLoginRepository {
    return DesignerNewsLoginRepository.getInstance(
            provideDesignerNewsLoginLocalDataSource(context),
            provideDesignerNewsLoginRemoteDataSource(context))
}

fun provideDesignerNewsLoginRemoteDataSource(context: Context): DesignerNewsLoginRemoteDataSource {
    // using a shared instance of the token holder between the remote data source and the service
    // so the remote data source can update the token without having to recreate the service
    // and at run time, having the service use the latest token
    // TODO right now, the token is held by the DesignerNewsLoginDataSource and updated via the
    // login repository. Preferably, only the remote data source should know how to get and store
    // the auth token
    val tokenHolder = provideDesignerNewsAuthTokenLocalDataSource(context)
    return DesignerNewsLoginRemoteDataSource(tokenHolder, provideDesignerNewsService(tokenHolder))
}

private fun provideDesignerNewsAuthTokenLocalDataSource(
    context: Context
): DesignerNewsAuthTokenLocalDataSource {
    return DesignerNewsAuthTokenLocalDataSource.getInstance(
            provideSharedPreferences(
                    context,
                    DesignerNewsAuthTokenLocalDataSource.DESIGNER_NEWS_AUTH_PREF))
}

fun provideDesignerNewsService(
    authTokenDataSource: DesignerNewsAuthTokenLocalDataSource
): DesignerNewsService {
    val client = OkHttpClient.Builder()
            .addInterceptor(
                    ClientAuthInterceptor(authTokenDataSource, BuildConfig.DESIGNER_NEWS_CLIENT_ID))
            .addInterceptor(loggingInterceptor)
            .build()
    val gson = Gson()
    return Retrofit.Builder()
            .baseUrl(DesignerNewsService.ENDPOINT)
            .client(client)
            .addConverterFactory(DenvelopingConverter(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(DesignerNewsService::class.java)
}

fun provideDesignerNewsRepository(context: Context): io.plaidapp.core.designernews.data.api.DesignerNewsRepository {
    return provideDesignerNewsRepository(DesignerNewsPrefs.get(context).api)
}

fun provideDesignerNewsRepository(service: DesignerNewsService): io.plaidapp.core.designernews.data.api.DesignerNewsRepository {
    return io.plaidapp.core.designernews.data.api.DesignerNewsRepository.getInstance(service)
}

fun provideDesignerNewsCommentsRepository(): DesignerNewsCommentsRepository {
    return DesignerNewsCommentsRepository.getInstance(provideDesignerNewsService())
}