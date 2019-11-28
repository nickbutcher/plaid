/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.core.dagger.designernews

import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Lazy
import dagger.Module
import dagger.Provides
import io.plaidapp.core.BuildConfig
import io.plaidapp.core.dagger.DesignerNewsApi
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.data.api.DeEnvelopingConverter
import io.plaidapp.core.designernews.data.api.ClientAuthInterceptor
import io.plaidapp.core.designernews.data.api.DesignerNewsSearchConverter
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.login.AuthTokenLocalDataSource
import io.plaidapp.core.designernews.data.login.LoginLocalDataSource
import io.plaidapp.core.designernews.data.login.LoginRemoteDataSource
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.data.stories.StoriesRemoteDataSource
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dagger module to provide data functionality for DesignerNews.
 */
@Module
class DesignerNewsDataModule {

    @Provides
    @FeatureScope
    fun provideLoginRepository(
        localSource: LoginLocalDataSource,
        remoteSource: LoginRemoteDataSource
    ): LoginRepository =
        LoginRepository.getInstance(localSource, remoteSource)

    @Provides
    @FeatureScope
    fun provideAuthTokenLocalDataSource(
        sharedPreferences: SharedPreferences
    ): AuthTokenLocalDataSource =
        AuthTokenLocalDataSource.getInstance(sharedPreferences)

    @Provides
    @DesignerNewsApi
    fun providePrivateOkHttpClient(
        upstream: OkHttpClient,
        tokenHolder: AuthTokenLocalDataSource
    ): OkHttpClient {
        return upstream.newBuilder()
            .addInterceptor(ClientAuthInterceptor(tokenHolder, BuildConfig.DESIGNER_NEWS_CLIENT_ID))
            .build()
    }

    @Provides
    @FeatureScope
    fun provideDesignerNewsService(
        @DesignerNewsApi client: Lazy<OkHttpClient>,
        gson: Gson
    ): DesignerNewsService {
        return Retrofit.Builder()
            .baseUrl(DesignerNewsService.ENDPOINT)
            .callFactory(client.get())
            .addConverterFactory(DeEnvelopingConverter(gson))
            .addConverterFactory(DesignerNewsSearchConverter.Factory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(DesignerNewsService::class.java)
    }

    @Provides
    @FeatureScope
    fun provideStoriesRepository(
        storiesRemoteDataSource: StoriesRemoteDataSource
    ): StoriesRepository =
        StoriesRepository.getInstance(storiesRemoteDataSource)

    @Provides
    @FeatureScope
    fun provideStoriesRemoteDataSource(service: DesignerNewsService): StoriesRemoteDataSource {
        return StoriesRemoteDataSource.getInstance(service)
    }
}
