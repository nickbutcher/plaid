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

package io.plaidapp.search.dagger

import android.app.Activity
import android.content.Context
import com.bumptech.glide.util.ViewPreloadSizeProvider
import dagger.Module
import dagger.Provides
import io.plaidapp.R
import io.plaidapp.core.dagger.DataManagerModule
import io.plaidapp.core.dagger.FilterAdapterModule
import io.plaidapp.core.dagger.OnDataLoadedModule
import io.plaidapp.core.dagger.dribbble.DribbbleDataModule
import io.plaidapp.core.data.BaseDataManager
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.pocket.PocketUtils
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.search.domain.SearchDataManager

@Module(
    includes = [
        DataManagerModule::class,
        DribbbleDataModule::class,
        FilterAdapterModule::class,
        OnDataLoadedModule::class
    ]
)
class SearchModule(private val activity: Activity) {
    @Provides
    fun context(): Context = activity

    @Provides
    fun activity(): Activity = activity

    @Provides
    fun columns(): Int = activity.resources.getInteger(R.integer.num_columns)

    @Provides
    fun viewPreloadSizeProvider() = ViewPreloadSizeProvider<Shot>()

    @Provides
    fun isPocketInstalled() = PocketUtils.isPocketInstalled(activity)

    @Provides
    fun provideSearchDataManager(
        onDataLoadedCallback: BaseDataManager.OnDataLoadedCallback<List<PlaidItem>>,
        shotsRepository: ShotsRepository
    ): SearchDataManager = SearchDataManager(
        context(),
        onDataLoadedCallback,
        shotsRepository
    )
}