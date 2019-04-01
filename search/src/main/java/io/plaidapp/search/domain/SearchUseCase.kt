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

package io.plaidapp.search.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.Result
import io.plaidapp.core.interfaces.SearchDataSourcesRegistry

class SearchUseCase(
    dataSourcesRegistry: SearchDataSourcesRegistry,
    query: String
) {

    private val dataSources = dataSourcesRegistry.buildSearchDataSources(query)

    private val _searchResult = MutableLiveData<List<PlaidItem>>()
    val searchResult: LiveData<List<PlaidItem>>
        get() = _searchResult

    suspend fun loadMore() {
        dataSources.forEach {
            val result = it.loadMore()
            if (result is Result.Success) {
                val lastResult = _searchResult.value.orEmpty().toMutableList()
                lastResult.addAll(result.data)
                _searchResult.postValue(lastResult)
            }
        }
    }
}
