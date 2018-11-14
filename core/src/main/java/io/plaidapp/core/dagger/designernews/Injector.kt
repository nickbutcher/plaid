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

@file:JvmName("Injector")

package io.plaidapp.core.dagger.designernews

import io.plaidapp.core.dagger.CoreDataModule
import io.plaidapp.core.dagger.CoroutinesDispatcherProviderModule
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.login.LoginLocalDataSource
import io.plaidapp.core.designernews.data.votes.UpvoteStoryService
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Injector for [UpvoteStoryService].
 */

fun inject(service: UpvoteStoryService) {
    val coreDataModule =
            CoreDataModule(DesignerNewsService.ENDPOINT, GsonConverterFactory.create())

    DaggerUpvoteStoryServiceComponent.builder()
            .coroutinesDispatcherProviderModule(CoroutinesDispatcherProviderModule())
            .coreDataModule(coreDataModule)
            .sharedPreferencesModule(
                    SharedPreferencesModule(service, LoginLocalDataSource.DESIGNER_NEWS_PREF)
            )
            .build()
            .inject(service)
}
