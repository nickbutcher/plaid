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

@file:JvmName("Injection")

package io.plaidapp.designernews

import android.content.Context
import com.google.gson.Gson
import io.plaidapp.BuildConfig
import io.plaidapp.data.api.ClientAuthInterceptor
import io.plaidapp.data.api.DenvelopingConverter
import io.plaidapp.designernews.data.api.DesignerNewsRepository
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.login.data.DesignerNewsLoginLocalStorage
import io.plaidapp.designernews.login.data.DesignerNewsLoginRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * File providing different dependencies.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */


fun provideDesignerNewsLoginLocalStorage(context: Context): DesignerNewsLoginLocalStorage {
    val preferences = context.applicationContext
            .getSharedPreferences(
                    DesignerNewsLoginLocalStorage.DESIGNER_NEWS_PREF,
                    Context.MODE_PRIVATE
            )
    return DesignerNewsLoginLocalStorage(preferences)
}

fun provideDesignerNewsLoginRepository(context: Context): DesignerNewsLoginRepository {
    return DesignerNewsLoginRepository.getInstance(provideDesignerNewsLoginLocalStorage(context))
}

fun provideDesignerNewsService(accessToken: String? = null): DesignerNewsService {
    val client = OkHttpClient.Builder()
            .addInterceptor(
                    ClientAuthInterceptor(accessToken, BuildConfig.DESIGNER_NEWS_CLIENT_ID))
            .addInterceptor(getHttpLoggingInterceptor())
            .build()
    val gson = Gson()
    return Retrofit.Builder()
            .baseUrl(DesignerNewsService.ENDPOINT)
            .client(client)
            .addConverterFactory(DenvelopingConverter(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(DesignerNewsService::class.java)
}

fun provideDesignerNewsRepository(context: Context): DesignerNewsRepository {
    return provideDesignerNewsRepository(DesignerNewsPrefs.get(context).api)
}

fun provideDesignerNewsRepository(service: DesignerNewsService): DesignerNewsRepository {
    return DesignerNewsRepository.getInstance(service)
}

private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val debugLevel = if (BuildConfig.DEBUG)
        HttpLoggingInterceptor.Level.BASIC
    else
        HttpLoggingInterceptor.Level.NONE
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = debugLevel
    return loggingInterceptor
}