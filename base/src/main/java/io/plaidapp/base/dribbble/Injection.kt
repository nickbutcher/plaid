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

package io.plaidapp.base.dribbble

import com.google.gson.GsonBuilder
import io.plaidapp.base.BuildConfig
import io.plaidapp.base.data.api.DenvelopingConverter
import io.plaidapp.base.data.api.dribbble.DribbbleService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val loggingInterceptor = HttpLoggingInterceptor().apply {
  level = if (BuildConfig.DEBUG) {
    BODY
  } else {
    NONE
  }
}

fun provideDribbbleService(): DribbbleService {
  val client = OkHttpClient.Builder()
      .addInterceptor(loggingInterceptor)
      .build()
  val gson = GsonBuilder()
      .setDateFormat(DribbbleService.DATE_FORMAT)
      .create()
  return Retrofit.Builder()
      .baseUrl(DribbbleService.ENDPOINT)
      .client(client)
      .addConverterFactory(DenvelopingConverter(gson))
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
      .create(DribbbleService::class.java)
}
