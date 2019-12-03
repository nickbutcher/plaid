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

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plaidapp.R
import io.plaidapp.core.dagger.DataManagerModule
import io.plaidapp.core.dagger.SourcesRepositoryModule
import io.plaidapp.core.dagger.dribbble.DribbbleDataModule
import io.plaidapp.core.dagger.qualifier.IsPocketInstalled
import io.plaidapp.core.data.pocket.PocketUtils
import io.plaidapp.core.ui.ConnectivityChecker
import io.plaidapp.ui.HomeActivity
import io.plaidapp.ui.HomeViewModel
import io.plaidapp.ui.HomeViewModelFactory

/**
 * Dagger module for [io.plaidapp.ui.HomeActivity].
 */
@Module(
        includes = [
            DataManagerModule::class,
            SourcesRepositoryModule::class,
            DribbbleDataModule::class
        ]
)
abstract class HomeModule {

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

        @IsPocketInstalled
        @JvmStatic
        @Provides
        fun isPocketInstalled(activity: Activity): Boolean = PocketUtils.isPocketInstalled(activity)

        @JvmStatic
        @Provides
        fun homeViewModel(
            factory: HomeViewModelFactory,
            fragmentActivity: FragmentActivity
        ): HomeViewModel {
            return ViewModelProvider(fragmentActivity, factory).get(HomeViewModel::class.java)
        }

        @JvmStatic
        @Provides
        fun connectivityChecker(activity: Activity): ConnectivityChecker? {
            val connectivityManager = activity.getSystemService<ConnectivityManager>()
            return if (connectivityManager != null) {
                ConnectivityChecker(connectivityManager)
            } else {
                null
            }
        }
    }
}
