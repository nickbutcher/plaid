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

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.util.ViewPreloadSizeProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plaidapp.R
import io.plaidapp.core.dagger.DataManagerModule
import io.plaidapp.core.dagger.OnDataLoadedModule
import io.plaidapp.core.dagger.SourcesRepositoryModule
import io.plaidapp.core.dagger.dribbble.DribbbleDataModule
import io.plaidapp.core.data.pocket.PocketUtils
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.ui.HomeActivity

/**
 * Dagger module for [io.plaidapp.ui.HomeActivity].
 */
@Module(
    includes = [
        DataManagerModule::class,
        SourcesRepositoryModule::class,
        DribbbleDataModule::class,
        OnDataLoadedModule::class
    ]
)
abstract class HomeModule(private val activity: Activity) {

    @Binds
    abstract fun homeActivityAsFragmentActivity(activity: HomeActivity): FragmentActivity

    @Binds
    abstract fun homeActivityAsActivity(activity: HomeActivity): Activity

    @Binds
    abstract fun context(activity: Activity): Context

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun columns(activity: Activity): Int = activity.resources.getInteger(R.integer.num_columns)

        @JvmStatic
        @Provides
        fun viewPreloadSizeProvider(): ViewPreloadSizeProvider<Shot> = ViewPreloadSizeProvider()

        @JvmStatic
        @Provides
        fun isPocketInstalled(activity: Activity): Boolean = PocketUtils.isPocketInstalled(activity)
    }
}
