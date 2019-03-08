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

package io.plaidapp.search.dagger

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plaidapp.R
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.core.dagger.designernews.DesignerNewsDataModule
import io.plaidapp.core.dagger.dribbble.DribbbleDataModule
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.data.pocket.PocketUtils
import io.plaidapp.search.domain.SearchDataManager
import io.plaidapp.search.ui.SearchActivity
import io.plaidapp.search.ui.SearchViewModel
import io.plaidapp.search.ui.SearchViewModelFactory

@Module(
    includes = [
        DribbbleDataModule::class,
        DesignerNewsDataModule::class,
        SharedPreferencesModule::class
    ]
)
abstract class SearchModule {

    @Binds
    abstract fun searchActivityAsAppCompatActivity(activity: SearchActivity): AppCompatActivity

    @Binds
    abstract fun searchActivityAsActivity(activity: SearchActivity): Activity

    @Binds
    abstract fun context(activity: Activity): Context

    @Binds
    abstract fun dataLoadingSubject(searchDataManager: SearchDataManager): DataLoadingSubject

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun columns(activity: Activity): Int = activity.resources.getInteger(R.integer.num_columns)

        @JvmStatic
        @Provides
        fun isPocketInstalled(activity: Activity) = PocketUtils.isPocketInstalled(activity)

        @JvmStatic
        @Provides
        fun searchViewModel(
            factory: SearchViewModelFactory,
            activity: AppCompatActivity
        ): SearchViewModel {
            return ViewModelProviders.of(activity, factory).get(SearchViewModel::class.java)
        }
    }
}
