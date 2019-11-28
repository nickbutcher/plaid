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

package io.plaidapp.search.domain

import io.plaidapp.core.interfaces.SearchDataSourceFactory
import javax.inject.Inject
import javax.inject.Provider

/**
 * Registry that holds all [SearchDataSourceFactory] available
 */
class SearchDataSourceFactoriesRegistry @Inject constructor(
    defaultFactories: Provider<Set<SearchDataSourceFactory>>
) {
    private val _dataSourceFactories = mutableSetOf<SearchDataSourceFactory>()

    init {
        defaultFactories.get()?.apply { _dataSourceFactories.addAll(this) }
    }

    val dataSourceFactories: Set<SearchDataSourceFactory>
        get() = _dataSourceFactories
}
