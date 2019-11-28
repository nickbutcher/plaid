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

import android.content.res.Resources
import dagger.Module
import dagger.Provides
import io.plaidapp.about.ui.AboutActivity
import io.plaidapp.about.ui.AboutStyler
import io.plaidapp.core.dagger.scope.FeatureScope

/**
 * Dagger module providing stuff for [AboutActivity].
 */
@Module class AboutActivityModule(private val activity: AboutActivity) {

    @Provides
    fun provideContext(): AboutActivity = activity

    @Provides
    fun provideResources(): Resources = activity.resources

    @Provides
    @FeatureScope
    fun provideAboutStyler(): AboutStyler = AboutStyler(activity)
}
