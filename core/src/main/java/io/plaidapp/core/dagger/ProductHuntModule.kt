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
import dagger.Lazy
import dagger.Module
import dagger.Provides
import io.plaidapp.core.BuildConfig
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.api.DeEnvelopingConverter
import io.plaidapp.core.producthunt.data.ProductHuntRemoteDataSource
import io.plaidapp.core.producthunt.data.api.AuthInterceptor
import io.plaidapp.core.producthunt.data.api.ProductHuntRepository
import io.plaidapp.core.producthunt.data.api.ProductHuntService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

@Retention(BINARY)
@Qualifier
private annotation class LocalApi

/**
 * Dagger module to provide injections for Product Hunt.
 */
@Module(includes = [CoreDataModule::class])
class ProductHuntModule {

    @Provides
    fun provideProductHuntRepository(
        remoteDataSource: ProductHuntRemoteDataSource,
        dispatcherProvider: CoroutinesDispatcherProvider
    ) = ProductHuntRepository.getInstance(remoteDataSource, dispatcherProvider)

    @LocalApi
    @Provides
    fun providePrivateOkHttpClient(upstreamClient: OkHttpClient): OkHttpClient {
        return upstreamClient.newBuilder()
            .addInterceptor(AuthInterceptor(BuildConfig.PRODUCT_HUNT_DEVELOPER_TOKEN))
            .build()
    }

    @Provides
    fun provideProductHuntService(
        @LocalApi okhttpClient: Lazy<OkHttpClient>,
        converterFactory: GsonConverterFactory,
        deEnvelopingConverter: DeEnvelopingConverter,
        callAdapterFactory: CoroutineCallAdapterFactory
    ): ProductHuntService {
        return createRetrofit(
            okhttpClient,
            converterFactory,
            deEnvelopingConverter,
            callAdapterFactory
        ).create(ProductHuntService::class.java)
    }

    private fun createRetrofit(
        okhttpClient: Lazy<OkHttpClient>,
        converterFactory: GsonConverterFactory,
        deEnvelopingConverter: DeEnvelopingConverter,
        callAdapterFactory: CoroutineCallAdapterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ProductHuntService.ENDPOINT)
            .callFactory { okhttpClient.get().newCall(it) }
            .addConverterFactory(deEnvelopingConverter)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()
    }
}
