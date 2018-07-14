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
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import io.plaidapp.core.BuildConfig
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.api.DenvelopingConverter
import io.plaidapp.core.designernews.data.api.ClientAuthInterceptor
import io.plaidapp.core.designernews.data.api.DesignerNewsAuthTokenLocalDataSource
import io.plaidapp.core.designernews.data.api.DesignerNewsRepository
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.api.comments.DesignerNewsCommentsRemoteDataSource
import io.plaidapp.core.designernews.data.api.comments.DesignerNewsCommentsRepository
import io.plaidapp.core.designernews.data.votes.DesignerNewsVotesRepository
import io.plaidapp.core.designernews.data.votes.VotesRemoteDataSource
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginLocalDataSource
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginRemoteDataSource
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginRepository
import io.plaidapp.core.loggingInterceptor
import io.plaidapp.core.provideCoroutinesContextProvider
import io.plaidapp.core.provideSharedPreferences
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

fun provideDesignerNewsLoginRepository(context: Context): DesignerNewsLoginRepository {
    return DesignerNewsLoginRepository.getInstance(
            provideDesignerNewsLoginLocalDataSource(context),
            provideDesignerNewsLoginRemoteDataSource(context))
}

fun provideDesignerNewsLoginRemoteDataSource(context: Context): DesignerNewsLoginRemoteDataSource {
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

fun provideDesignerNewsService(context: Context): DesignerNewsService {
    val tokenHolder = provideDesignerNewsAuthTokenLocalDataSource(context)
    return provideDesignerNewsService(tokenHolder)
}

private fun provideDesignerNewsService(
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

fun provideDesignerNewsRepository(context: Context): DesignerNewsRepository {
    return provideDesignerNewsRepository(DesignerNewsPrefs.get(context).api)
}

private fun provideDesignerNewsRepository(service: DesignerNewsService): DesignerNewsRepository {
    return DesignerNewsRepository.getInstance(service)
}

fun provideDesignerNewsCommentsRepository(context: Context): DesignerNewsCommentsRepository {
    return provideDesignerNewsCommentsRepository(
            provideDesignerNewsCommentsRemoteDataSource(provideDesignerNewsService(context)),
            provideCoroutinesContextProvider())
}

private fun provideDesignerNewsCommentsRepository(
    remoteDataSource: DesignerNewsCommentsRemoteDataSource,
    contextProvider: CoroutinesContextProvider
): DesignerNewsCommentsRepository {
    return DesignerNewsCommentsRepository.getInstance(remoteDataSource, contextProvider)
}

private fun provideDesignerNewsCommentsRemoteDataSource(service: DesignerNewsService) =
        DesignerNewsCommentsRemoteDataSource.getInstance(service)

fun provideDesignerNewsVotesRepository(context: Context): DesignerNewsVotesRepository {
    return provideDesignerNewsVotesRepository(
            provideVotesRemoteDataSource(provideDesignerNewsService(context)),
            provideCoroutinesContextProvider()
    )
}

private fun provideVotesRemoteDataSource(service: DesignerNewsService) = VotesRemoteDataSource(service)

private fun provideDesignerNewsVotesRepository(
    remoteDataSource: VotesRemoteDataSource,
    contextProvider: CoroutinesContextProvider
): DesignerNewsVotesRepository {
    return DesignerNewsVotesRepository.getInstance(remoteDataSource, contextProvider)
}
