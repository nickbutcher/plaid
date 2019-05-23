/*
 * Copyright 2019 Google, Inc.
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

package io.plaidapp.designernews.dagger

import dagger.BindsInstance
import dagger.Component
import io.plaidapp.core.dagger.BaseActivityComponent
import io.plaidapp.core.dagger.CoreComponent
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.core.dagger.designernews.DesignerNewsDataModule
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.interfaces.PlaidDataSource
import io.plaidapp.dagger.HomeModule
import io.plaidapp.ui.HomeActivity

@Component(
    modules = [
        DataModule::class,
        DesignerNewsDataModule::class,
        HomeModule::class,
        FeedDataModule::class,
        SharedPreferencesModule::class
    ],
    dependencies = [CoreComponent::class]
)

@FeatureScope
interface DesignerNewsFeedComponent : BaseActivityComponent<HomeActivity> {

    fun defaultFeedDataSources(): Set<PlaidDataSource>

    @Component.Builder
    interface Builder {
        fun build(): DesignerNewsFeedComponent
        @BindsInstance
        fun homeActivity(activity: HomeActivity): Builder

        fun sharedPreferencesModule(module: SharedPreferencesModule): Builder
        fun coreComponent(component: CoreComponent): Builder
    }
}
