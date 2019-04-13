/*
 * Copyright 2019 Google, Inc.
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

package io.plaidapp.core.interfaces

import javax.inject.Inject
import javax.inject.Provider

class SearchDataSourceFactoriesRegistry @Inject constructor(
    defaultFactories: Provider<List<SearchDataSourceFactory>>
) {
    private val _dataSourceFactories = mutableListOf<SearchDataSourceFactory>()

    init {
        defaultFactories.get()?.apply { _dataSourceFactories.addAll(this) }
    }

    val dataSourceFactories: List<SearchDataSourceFactory>
        get() = _dataSourceFactories

    fun add(dataSourceFactory: SearchDataSourceFactory) {
        if (_dataSourceFactories.contains(dataSourceFactory)) return
        _dataSourceFactories.add(dataSourceFactory)
    }

    fun remove(dataSourceFactory: SearchDataSourceFactory) {
        if (_dataSourceFactories.contains(dataSourceFactory)) return
        _dataSourceFactories.remove(dataSourceFactory)
    }
}
