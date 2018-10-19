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

package io.plaidapp.core.dagger.dribbble

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import io.plaidapp.core.BuildConfig
import io.plaidapp.core.dagger.CoroutinesDispatcherProviderModule
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.search.DribbbleSearchConverter
import io.plaidapp.core.dribbble.data.search.DribbbleSearchService
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * Dagger module providing classes required to dribbble with data.
 */
@Module(includes = [CoroutinesDispatcherProviderModule::class])
class DribbbleDataModule {

    @Provides
    fun provideShotsRepository(
        remoteDataSource: SearchRemoteDataSource,
        dispatcherProvider: CoroutinesDispatcherProvider
    ) = ShotsRepository.getInstance(remoteDataSource, dispatcherProvider)

    @Provides
    fun provideDribbbleSearchService(): DribbbleSearchService =
        provideRetrofit(
            DribbbleSearchService.ENDPOINT,
            provideDribbleSearchConverterFactory()
        )
            .create(DribbbleSearchService::class.java)

    @Provides
    fun provideRetrofit(
        baseUrl: String,
        factory: Converter.Factory
    ): Retrofit {
        return provideRetrofitBuilder()
            .baseUrl(baseUrl)
            .addConverterFactory(factory)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(provideOkHttpClient(provideLoggingInterceptor()))
            .build()
    }

    @Provides
    fun provideRetrofitBuilder() = Retrofit.Builder()

    @Provides
    fun provideDribbleSearchConverterFactory() = DribbbleSearchConverter.Factory()

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
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
}
