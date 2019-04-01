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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DataSourcesRegistry(defaultDataSources: List<PlaidDataSource>) {

    private val _dataSources = MutableLiveData<List<PlaidDataSource>>(defaultDataSources)

    val dataSources: LiveData<List<PlaidDataSource>>
        get() = _dataSources

    fun add(dataSource: PlaidDataSource) {
        val existingDataSources = _dataSources.value.orEmpty().toMutableList()
        existingDataSources.add(dataSource)
        _dataSources.postValue(existingDataSources)
    }

    fun remove(dataSource: PlaidDataSource) {
        val existingDataSources = _dataSources.value.orEmpty().toMutableList()
        existingDataSources.remove(dataSource)
        _dataSources.postValue(existingDataSources)
    }
}
