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

import dagger.Module
import dagger.Provides
import io.plaidapp.core.dagger.CoreDataModule
import io.plaidapp.core.dagger.CoroutinesDispatcherProviderModule
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.ManualInjection
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.search.DribbbleSearchConverter
import io.plaidapp.core.dribbble.data.search.DribbbleSearchService
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource

/**
 * Dagger module providing classes required to dribbble with data.
 */
@Module(includes = [CoreDataModule::class, CoroutinesDispatcherProviderModule::class])
class DribbbleDataModule {

    @Provides
    fun provideShotsRepository(
        remoteDataSource: SearchRemoteDataSource,
        dispatcherProvider: CoroutinesDispatcherProvider
    ) = ShotsRepository.getInstance(remoteDataSource, dispatcherProvider)

    @Provides
    fun provideDribbbleSearchService(): DribbbleSearchService =
        ManualInjection.retrofit(
            DribbbleSearchService.ENDPOINT,
            DribbbleSearchConverter.Factory()
        ).create(DribbbleSearchService::class.java)
}
