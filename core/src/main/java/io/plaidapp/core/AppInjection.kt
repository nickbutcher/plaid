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

@file:JvmName("AppInjection")

package io.plaidapp.core

import android.content.Context
import android.content.SharedPreferences
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import kotlinx.coroutines.Dispatchers
import okhttp3.logging.HttpLoggingInterceptor

/**
 * File providing different dependencies that are shared at the app level.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */

val debugLevel = if (BuildConfig.DEBUG) {
    HttpLoggingInterceptor.Level.BODY
} else {
    HttpLoggingInterceptor.Level.NONE
}

@Deprecated("Use Dagger LoggingInterceptorModule instead")
val loggingInterceptor = HttpLoggingInterceptor().apply { level = debugLevel }

@Deprecated("Use Dagger SharedPreferenceModule instead")
fun provideSharedPreferences(context: Context, name: String): SharedPreferences {
    return context.applicationContext
            .getSharedPreferences(name, Context.MODE_PRIVATE)
}

@Deprecated("Use Dagger CoroutinesDispatcherProviderModule instead")
fun provideCoroutinesDispatcherProvider() = CoroutinesDispatcherProvider(
    Dispatchers.Main,
    Dispatchers.Default,
    Dispatchers.IO
)
