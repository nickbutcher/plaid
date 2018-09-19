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

import dagger.Component
import io.plaidapp.core.dagger.CoreComponent
import io.plaidapp.core.dagger.DataManagerModule
import io.plaidapp.core.dagger.FilterAdapterModule
import io.plaidapp.core.dagger.OnDataLoadedModule
import io.plaidapp.ui.HomeActivity

/**
 * Dagger component for the [HomeActivity].
 */
@Component(modules = [HomeModule::class], dependencies = [CoreComponent::class])
interface HomeComponent {

    fun inject(activity: HomeActivity)

    @Component.Builder
    interface Builder {

        fun build(): HomeComponent
        fun coreComponent(module: CoreComponent): Builder
        fun dataManagerModule(module: DataManagerModule): Builder
        fun dataLoadedModule(module: OnDataLoadedModule): Builder
        fun filterAdapterModule(module: FilterAdapterModule): Builder
        fun homeModule(module: HomeModule): Builder
    }
}
