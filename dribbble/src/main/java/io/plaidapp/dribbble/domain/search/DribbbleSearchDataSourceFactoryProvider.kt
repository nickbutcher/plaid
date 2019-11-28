/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.dribbble.domain.search

import android.content.Context
import io.plaidapp.core.interfaces.SearchDataSourceFactory
import io.plaidapp.core.interfaces.SearchDataSourceFactoryProvider
import io.plaidapp.dribbble.dagger.DaggerDribbbleSearchComponent
import io.plaidapp.ui.PlaidApplication.Companion.coreComponent

/**
 * Provider for Dribbble implementations of [SearchDataSourceFactory]
 */
class DribbbleSearchDataSourceFactoryProvider : SearchDataSourceFactoryProvider {

    /**
     * To construct the concrete implementation of [SearchDataSourceFactory], we need to build the
     * dependency graph
     */
    override fun getFactory(context: Context): SearchDataSourceFactory {
        return DaggerDribbbleSearchComponent.builder()
            .coreComponent(coreComponent(context))
            .build().factory()
    }
}
