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

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import io.plaidapp.core.dagger.CoroutinesContextProviderModule
import io.plaidapp.core.dagger.DataManagerModule
import io.plaidapp.core.dagger.DribbbleDataModule
import io.plaidapp.core.dagger.DribbbleRetrofitModule
import io.plaidapp.core.dagger.FilterAdapterModule
import io.plaidapp.core.dagger.OnDataLoadedModule
import io.plaidapp.core.dagger.DribbleSearchServiceProvider
import io.plaidapp.core.dagger.ShotsRepositoryModule
import io.plaidapp.ui.HomeActivity

@Component(modules = [HomeModule::class, ShotsRepositoryModule::class])
interface HomeComponent {

    fun inject(activity: HomeActivity)

    @Component.Builder
    interface Builder {
        fun build(): HomeComponent

        @BindsInstance
        fun context(context: Context): Builder

        fun coroutinesContextProviderModule(module: CoroutinesContextProviderModule): Builder
        fun dataLoadedModule(module: OnDataLoadedModule): Builder
        fun dataManagerModule(module: DataManagerModule): Builder
        fun dribbbleDataModule(module: DribbbleDataModule): Builder
        fun retrofitModule(module: DribbbleRetrofitModule): Builder
        fun homeModule(module: HomeModule): Builder
        fun filterAdapterModule(module: FilterAdapterModule): Builder
        fun searchRemoteDataSourceModule(module: DribbleSearchServiceProvider): Builder
        fun shotsRepositoryModule(module: ShotsRepositoryModule): Builder
    }
}
