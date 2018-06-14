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

package io.plaidapp.base.designernews

import android.content.Context
import com.google.gson.Gson
import io.plaidapp.base.BuildConfig
import io.plaidapp.base.data.api.ClientAuthInterceptor
import io.plaidapp.base.data.api.DenvelopingConverter
import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginLocalStorage
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRepository
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

fun provideDesignerNewsRepository(context: Context): io.plaidapp.base.designernews.data.api.DesignerNewsRepository {
    return provideDesignerNewsRepository(DesignerNewsPrefs.get(context).api)
}

fun provideDesignerNewsRepository(service: DesignerNewsService): io.plaidapp.base.designernews.data.api.DesignerNewsRepository {
    return io.plaidapp.base.designernews.data.api.DesignerNewsRepository.getInstance(service)
}

private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val debugLevel = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
    return HttpLoggingInterceptor()
            .also { it.level = debugLevel }
}