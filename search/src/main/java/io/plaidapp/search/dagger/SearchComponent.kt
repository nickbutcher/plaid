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
import dagger.BindsInstance
import dagger.Component
import io.plaidapp.core.dagger.CoreComponent
import io.plaidapp.core.dagger.DataManagerModule
import io.plaidapp.core.dagger.FilterAdapterModule
import io.plaidapp.core.dagger.OnDataLoadedModule
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.search.ui.SearchActivity

/**
 * Dagger component for the [SearchActivity].
 */
@Component(modules = [SearchModule::class], dependencies = [CoreComponent::class])
interface SearchComponent {

    fun inject(activity: SearchActivity)

    @Component.Builder
    interface Builder {

        fun build(): SearchComponent
        @BindsInstance fun activity(activity: Activity): Builder
        fun coreComponent(module: CoreComponent): Builder
        fun dataManagerModule(module: DataManagerModule): Builder
        fun dataLoadedModule(module: OnDataLoadedModule): Builder
        fun filterAdapterModule(module: FilterAdapterModule): Builder
        fun sharedPreferencesModule(module: SharedPreferencesModule): Builder
        fun searchModule(module: SearchModule): Builder
    }
}
