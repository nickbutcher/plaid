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

package io.plaidapp.designernews.dagger

import dagger.Component
import io.plaidapp.core.dagger.BaseComponent
import io.plaidapp.core.dagger.CoreComponent
import io.plaidapp.core.dagger.CoreDataModule
import io.plaidapp.core.dagger.CoroutinesDispatcherProviderModule
import io.plaidapp.core.dagger.MarkdownModule
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.designernews.ui.story.StoryActivity

/**
 * Dagger component for [StoryActivity].
 */
@Component(modules = [StoryModule::class], dependencies = [CoreComponent::class])
interface StoryComponent : BaseComponent<StoryActivity> {

    @Component.Builder
    interface Builder {
        fun build(): StoryComponent
        fun coreComponent(coreComponent: CoreComponent): Builder
        fun coroutinesDispatcherProviderModule(module: CoroutinesDispatcherProviderModule): Builder
        fun coreDataModule(module: CoreDataModule): Builder
        fun designerNewsModule(module: StoryModule): Builder
        fun markdownModule(module: MarkdownModule): Builder
        fun sharedPreferencesModule(module: SharedPreferencesModule): Builder
    }
}
