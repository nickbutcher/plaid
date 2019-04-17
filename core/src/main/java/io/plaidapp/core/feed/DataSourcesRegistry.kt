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

package io.plaidapp.core.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.plaidapp.core.interfaces.PlaidDataSource

class DataSourcesRegistry(
    defaultDataSources: Set<PlaidDataSource>
) {

    private val _dataSources: MutableLiveData<Set<PlaidDataSource>> =
        MutableLiveData(defaultDataSources)
    val dataSources: LiveData<Set<PlaidDataSource>>
        get() = _dataSources

    fun addDataSource(dataSource: PlaidDataSource) {
        val sources = _dataSources.value?.toMutableSet()
        if (sources != null && !sources.contains(dataSource)) {
            sources.add(dataSource)
            _dataSources.postValue(sources)
        }
    }

    fun removeDataSource(dataSource: PlaidDataSource) {
        val sources = _dataSources.value?.toMutableSet()
        if (sources != null && sources.contains(dataSource)) {
            sources.remove(dataSource)
            _dataSources.postValue(sources)
        }
    }
}
