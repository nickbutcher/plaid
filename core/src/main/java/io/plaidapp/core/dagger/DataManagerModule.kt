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

import dagger.Module
import dagger.Provides
import io.plaidapp.core.dagger.designernews.DesignerNewsDataModule
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.data.DataManager
import io.plaidapp.core.data.OnDataLoadedCallback
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.designernews.domain.LoadStoriesUseCase
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.producthunt.data.api.ProductHuntRepository
import io.plaidapp.core.ui.filter.FilterAdapter

/**
 * Module to provide [DataManager].
 */
@Module(includes = [DesignerNewsDataModule::class, ProductHuntModule::class])
class DataManagerModule {

    private lateinit var manager: DataManager

    @Provides
    fun provideDataManager(
        onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>,
        loadStoriesUseCase: LoadStoriesUseCase,
        searchStoriesUseCase: SearchStoriesUseCase,
        productHuntRepository: ProductHuntRepository,
        shotsRepository: ShotsRepository,
        filterAdapter: FilterAdapter
    ): DataManager = getDataManager(
        onDataLoadedCallback,
        loadStoriesUseCase,
        productHuntRepository,
        searchStoriesUseCase,
        shotsRepository,
        filterAdapter
    )

    @Provides
    fun provideDataLoadingSubject(
        onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>,
        loadStoriesUseCase: LoadStoriesUseCase,
        productHuntRepository: ProductHuntRepository,
        searchStoriesUseCase: SearchStoriesUseCase,
        shotsRepository: ShotsRepository,
        filterAdapter: FilterAdapter
    ): DataLoadingSubject = getDataManager(
        onDataLoadedCallback,
        loadStoriesUseCase,
        productHuntRepository,
        searchStoriesUseCase,
        shotsRepository,
        filterAdapter
    )

    private fun getDataManager(
        onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>,
        loadStoriesUseCase: LoadStoriesUseCase,
        productHuntRepository: ProductHuntRepository,
        searchStoriesUseCase: SearchStoriesUseCase,
        shotsRepository: ShotsRepository,
        filterAdapter: FilterAdapter
    ): DataManager {
        return if (::manager.isInitialized) {
            manager
        } else {
            manager = DataManager(
                onDataLoadedCallback,
                loadStoriesUseCase,
                productHuntRepository,
                searchStoriesUseCase,
                shotsRepository,
                filterAdapter
            )
            manager
        }
    }
}
