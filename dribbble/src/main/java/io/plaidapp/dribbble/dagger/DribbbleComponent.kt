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

package io.plaidapp.dribbble.dagger

import dagger.BindsInstance
import dagger.Component
import io.plaidapp.core.dagger.CoreComponent
import io.plaidapp.core.dagger.dribbble.DribbbleDataModule
import io.plaidapp.dribbble.ui.shot.ShotActivity

/**
 * Component binding injections for the :dribbble feature module.
 */
@Component(
    modules = [DribbbleModule::class, DribbbleDataModule::class],
    dependencies = [CoreComponent::class]
)
interface DribbbleComponent {

    fun inject(activity: ShotActivity)

    @Component.Builder
    interface Builder {

        fun build(): DribbbleComponent
        @BindsInstance fun activity(activity: ShotActivity): Builder
        fun coreComponent(component: CoreComponent): Builder
        fun dribbbleModule(module: DribbbleModule): Builder
    }
}
