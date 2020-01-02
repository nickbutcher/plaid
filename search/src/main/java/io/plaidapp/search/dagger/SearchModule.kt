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

package io.plaidapp.search.dagger

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.plaidapp.R
import io.plaidapp.core.dagger.qualifier.IsPocketInstalled
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.data.pocket.PocketUtils
import io.plaidapp.core.interfaces.SearchDataSourceFactory
import io.plaidapp.core.interfaces.SearchDataSourceFactoryProvider
import io.plaidapp.search.ui.SearchActivity
import io.plaidapp.search.ui.SearchViewModel
import io.plaidapp.search.ui.SearchViewModelFactory
import kotlin.reflect.full.createInstance

@Module
abstract class SearchModule {

    @Binds
    abstract fun searchActivityAsAppCompatActivity(activity: SearchActivity): AppCompatActivity

    @Binds
    abstract fun searchActivityAsActivity(activity: SearchActivity): Activity

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
        @FeatureScope
        fun factories(activity: Activity): Set<SearchDataSourceFactory> {
            val factories = mutableSetOf<SearchDataSourceFactory>()

            searchDataSourceFactory(
                activity,
                "io.plaidapp.designernews.domain.search.DesignerNewsSearchDataSourceFactoryProvider"
            )?.apply { factories.add(this) }

            searchDataSourceFactory(
                activity,
                "io.plaidapp.dribbble.domain.search.DribbbleSearchDataSourceFactoryProvider"
            )?.apply { factories.add(this) }

            return factories
        }

        private fun searchDataSourceFactory(
            context: Context,
            className: String
        ): SearchDataSourceFactory? {
            return try {
                val provider =
                    Class.forName(className).kotlin.createInstance() as SearchDataSourceFactoryProvider
                provider.getFactory(context)
            } catch (e: ClassNotFoundException) {
                null
            }
        }

        @JvmStatic
        @Provides
        fun searchViewModel(
            factory: SearchViewModelFactory,
            activity: AppCompatActivity
        ): SearchViewModel {
            return ViewModelProvider(activity, factory).get(SearchViewModel::class.java)
        }
    }
}
