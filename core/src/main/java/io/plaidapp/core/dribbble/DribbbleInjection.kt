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

@file:JvmName("DribbbleInjection")

package io.plaidapp.core.dribbble

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.dribbble.data.search.DribbbleSearchConverter
import io.plaidapp.core.dribbble.data.search.DribbbleSearchService
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource
import io.plaidapp.core.loggingInterceptor
import io.plaidapp.core.provideCoroutinesContextProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * File providing different dependencies for Dribbble.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */

fun provideShotsRepository() = provideShotsRepository(
    provideSearchRemoteDataSource(provideDribbbleSearchService()),
    provideCoroutinesContextProvider()
)

private fun provideDribbbleSearchService(): DribbbleSearchService {
    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    return Retrofit.Builder()
        .baseUrl(DribbbleSearchService.ENDPOINT)
        .addConverterFactory(DribbbleSearchConverter.Factory())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(client)
        .build()
        .create(DribbbleSearchService::class.java)
}

private fun provideShotsRepository(
    remoteDataSource: SearchRemoteDataSource,
    contextProvider: CoroutinesContextProvider
) = ShotsRepository.getInstance(remoteDataSource, contextProvider)

private fun provideSearchRemoteDataSource(service: DribbbleSearchService) =
    SearchRemoteDataSource.getInstance(service)
