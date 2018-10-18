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

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import io.plaidapp.core.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Dagger module to provide core data functionality.
 */
@Module
class CoreDataModule(private val baseUrl: String, private val factory: Converter.Factory) {

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(provideLoggingInterceptor()).build()

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(factory)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
    }
}
