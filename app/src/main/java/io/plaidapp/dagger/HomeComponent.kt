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

package io.plaidapp.dagger

import dagger.BindsInstance
import dagger.Component
import io.plaidapp.core.dagger.BaseActivityComponent
import io.plaidapp.core.dagger.CoreComponent
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.ui.HomeActivity

/**
 * Dagger component for the [HomeActivity].
 */
@Component(
        modules = [HomeModule::class, SharedPreferencesModule::class],
        dependencies = [CoreComponent::class]
)
@FeatureScope
interface HomeComponent : BaseActivityComponent<HomeActivity> {

    @Component.Builder
    interface Builder {

        fun build(): HomeComponent
        @BindsInstance fun homeActivity(activity: HomeActivity): Builder
        fun coreComponent(module: CoreComponent): Builder
        fun sharedPreferencesModule(module: SharedPreferencesModule): Builder
    }
}
