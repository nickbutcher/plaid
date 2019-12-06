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

package io.plaidapp.core.dagger

import com.google.gson.Gson
import dagger.Component
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Component providing application wide singletons.
 * To call this make use of PlaidApplication.coreComponent or the
 * Activity.coreComponent extension function.
 */
@Component(modules = [CoreDataModule::class])
@Singleton
interface CoreComponent {

    @Component.Builder interface Builder {
        fun build(): CoreComponent
    }

    fun provideOkHttpClient(): OkHttpClient
    fun provideGson(): Gson
    fun provideGsonConverterFactory(): GsonConverterFactory
}
