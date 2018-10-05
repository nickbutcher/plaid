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

package io.plaidapp.core.dagger

import dagger.Component
import io.plaidapp.core.dagger.designernews.DesignerNewsDataModule
import io.plaidapp.core.dagger.dribbble.DribbbleDataModule

/**
 * Component providing application wide singletons.
 * To call this make use of PlaidApplication.coreComponent.
 */
@Component(
    modules = [
        DribbbleDataModule::class,
        DesignerNewsDataModule::class,
        MarkdownModule::class,
        SharedPreferencesModule::class
    ]
)
interface CoreComponent {

    @Component.Builder interface Builder {
        fun build(): CoreComponent
        fun markdownModule(module: MarkdownModule): Builder
        fun sharedPreferencesModuleModule(module: SharedPreferencesModule): Builder
    }
}
