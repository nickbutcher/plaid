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

package io.plaidapp.about.dagger

import dagger.BindsInstance
import dagger.Component
import io.plaidapp.about.ui.AboutActivity
import io.plaidapp.core.dagger.BaseActivityComponent
import io.plaidapp.core.dagger.MarkdownModule
import io.plaidapp.core.dagger.scope.FeatureScope

/**
 * Dagger component for `about` feature module.
 */
@Component(modules = [AboutActivityModule::class, MarkdownModule::class])
@FeatureScope
interface AboutComponent : BaseActivityComponent<AboutActivity> {

    @Component.Builder
    interface Builder {

        fun build(): AboutComponent

        @BindsInstance fun activity(activity: AboutActivity): Builder

        fun aboutActivityModule(module: AboutActivityModule): Builder

        fun markdownModule(module: MarkdownModule): Builder
    }
}
