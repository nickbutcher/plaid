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

package io.plaidapp.dagger

import io.plaidapp.core.dagger.CoroutinesContextProviderModule
import io.plaidapp.core.dagger.DataManagerModule
import io.plaidapp.core.dagger.dribbble.DribbbleDataModule
import io.plaidapp.core.dagger.dribbble.DribbbleRetrofitModule
import io.plaidapp.core.dagger.FilterAdapterModule
import io.plaidapp.core.dagger.OnDataLoadedModule
import io.plaidapp.core.dagger.dribbble.DribbleSearchServiceProvider
import io.plaidapp.core.dagger.ShotsRepositoryModule
import io.plaidapp.core.data.BaseDataManager
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.ui.HomeActivity

/**
 * Injector for HomeActivity.
 *
 * TODO: Convert to extension function once [HomeActivity] is converted to Kotlin.
 */
object Injector {

    @JvmStatic
    fun inject(
        activity: HomeActivity,
        dataLoadedCallback: BaseDataManager.OnDataLoadedCallback<List<PlaidItem>>
    ) {
        DaggerHomeComponent.builder()
            .context(activity)
            .coroutinesContextProviderModule(CoroutinesContextProviderModule())
            .dataLoadedModule(OnDataLoadedModule(dataLoadedCallback))
            .dataManagerModule(DataManagerModule(activity))
            .dribbbleDataModule(DribbbleDataModule())
            .homeModule(HomeModule(activity))
            .filterAdapterModule(FilterAdapterModule(activity))
            .retrofitModule(DribbbleRetrofitModule())
            .searchRemoteDataSourceModule(DribbleSearchServiceProvider())
            .shotsRepositoryModule(ShotsRepositoryModule())
            .build()
            .inject(activity)
    }
}
