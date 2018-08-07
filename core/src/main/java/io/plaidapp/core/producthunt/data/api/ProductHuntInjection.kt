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

@file:JvmName("ProductHuntInjection")

package io.plaidapp.core.producthunt.data.api

import com.google.gson.Gson
import io.plaidapp.core.BuildConfig
import io.plaidapp.core.data.api.DenvelopingConverter
import io.plaidapp.core.loggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * File providing different dependencies for Product Hunt.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */

fun provideProductHuntService(): ProductHuntService {
    val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(BuildConfig.PRODUCT_HUNT_DEVELOPER_TOKEN))
            .addInterceptor(loggingInterceptor)
            .build()
    val gson = Gson()
    return Retrofit.Builder()
            .baseUrl(ProductHuntService.ENDPOINT)
            .client(client)
            .addConverterFactory(DenvelopingConverter(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ProductHuntService::class.java)
}

fun provideProductHuntRepository() = ProductHuntRepository.getInstance(provideProductHuntService())
