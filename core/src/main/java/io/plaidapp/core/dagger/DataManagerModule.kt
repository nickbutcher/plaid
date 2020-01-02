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

import dagger.Module
import dagger.Provides
import io.plaidapp.core.dagger.designernews.DesignerNewsDataModule
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.data.DataManager
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.domain.LoadStoriesUseCase
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.producthunt.domain.LoadPostsUseCase

/**
 * Module to provide [DataManager].
 */
@Module(includes = [DesignerNewsDataModule::class, ProductHuntModule::class])
class DataManagerModule {

    @Provides
    @FeatureScope
    fun provideDataManager(
        loadStories: LoadStoriesUseCase,
        searchStories: SearchStoriesUseCase,
        loadPosts: LoadPostsUseCase,
        shotsRepository: ShotsRepository,
        sourcesRepository: SourcesRepository,
        coroutinesDispatcherProvider: CoroutinesDispatcherProvider
    ): DataManager = getDataManager(
        loadStories,
        loadPosts,
        searchStories,
        shotsRepository,
        sourcesRepository,
        coroutinesDispatcherProvider
    )

    @Provides
    @FeatureScope
    fun provideDataLoadingSubject(
        loadStories: LoadStoriesUseCase,
        loadPosts: LoadPostsUseCase,
        searchStories: SearchStoriesUseCase,
        shotsRepository: ShotsRepository,
        sourcesRepository: SourcesRepository,
        coroutinesDispatcherProvider: CoroutinesDispatcherProvider
    ): DataLoadingSubject = getDataManager(
        loadStories,
        loadPosts,
        searchStories,
        shotsRepository,
        sourcesRepository,
        coroutinesDispatcherProvider
    )

    private fun getDataManager(
        loadStories: LoadStoriesUseCase,
        loadPosts: LoadPostsUseCase,
        searchStories: SearchStoriesUseCase,
        shotsRepository: ShotsRepository,
        sourcesRepository: SourcesRepository,
        coroutinesDispatcherProvider: CoroutinesDispatcherProvider
    ): DataManager {
        return DataManager(
            loadStories,
            loadPosts,
            searchStories,
            shotsRepository,
            sourcesRepository,
            coroutinesDispatcherProvider
        )
    }
}
