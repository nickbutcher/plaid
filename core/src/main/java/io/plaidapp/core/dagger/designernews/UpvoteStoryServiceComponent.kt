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

package io.plaidapp.core.dagger.designernews

import dagger.Component
import io.plaidapp.core.dagger.BaseServiceComponent
import io.plaidapp.core.dagger.CoreComponent
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.designernews.data.votes.UpvoteStoryService

/**
 * Dagger component for the [UpvoteStoryServiceModule].
 */
@Component(
    modules = [
        UpvoteStoryServiceModule::class,
        DesignerNewsDataModule::class,
        SharedPreferencesModule::class
    ],
    dependencies = [CoreComponent::class]
)
@FeatureScope
interface UpvoteStoryServiceComponent : BaseServiceComponent<UpvoteStoryService> {

    @Component.Builder
    interface Builder {
        fun build(): UpvoteStoryServiceComponent
        fun coreComponent(component: CoreComponent): Builder
        fun sharedPreferencesModule(module: SharedPreferencesModule): Builder
        fun upvoteServiceModule(module: UpvoteStoryServiceModule): Builder
    }
}
