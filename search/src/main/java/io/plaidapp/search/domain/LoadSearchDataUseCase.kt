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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.interfaces.SearchDataSourceFactory
import io.plaidapp.core.ui.getPlaidItemsForDisplay
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Searches for a query in a list of data sources. Exposes the results of the search in a LiveData,
 * that is updated whenever loading of search results is requested.
 * The results of loading more are appended at the end of the list.
 */
class LoadSearchDataUseCase(
    factories: Set<SearchDataSourceFactory>,
    query: String
) {

    private val dataSources = factories.map { it.create(query) }

    private val _searchResult = MediatorLiveData<List<PlaidItem>>()
    val searchResult: LiveData<List<PlaidItem>>
        get() = _searchResult

    init {
        dataSources.forEach {
            _searchResult.addSource(it.items) { newList ->
                handleNewList(newList)
            }
        }
    }

    suspend operator fun invoke() {
        val deferredJobs = mutableListOf<Deferred<Unit>>()
        supervisorScope {
            dataSources.forEach {
                deferredJobs.add(async { it.loadMore() })
            }
        }
        deferredJobs.awaitAll()
    }

    private fun handleNewList(newList: List<PlaidItem>) {
        val oldItems = _searchResult.value.orEmpty().toMutableList()
        val searchResult = getPlaidItemsForDisplay(oldItems, newList)
        _searchResult.postValue(searchResult)
    }
}
