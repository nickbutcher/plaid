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

package io.plaidapp.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.data.OnDataLoadedCallback
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.util.event.Event
import io.plaidapp.search.domain.SearchDataManager
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val dataManager: SearchDataManager
) : ViewModel() {

    private val _searchResults = MutableLiveData<Event<List<PlaidItem>>>()
    val searchResults: LiveData<Event<List<PlaidItem>>>
        get() = _searchResults

    private val onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>> =
        object : OnDataLoadedCallback<List<PlaidItem>> {
            override fun onDataLoaded(data: List<PlaidItem>) {
                _searchResults.value = Event(data)
            }
        }

    init {
        dataManager.onDataLoadedCallback = onDataLoadedCallback
    }

    fun searchFor(query: String) {
        dataManager.searchFor(query)
    }

    fun loadMore() {
        dataManager.loadMore()
    }

    override fun onCleared() {
        super.onCleared()
        dataManager.cancelLoading()
    }

    fun getQuery() = dataManager.query

    fun clearResults() {
        dataManager.clear()
    }

    fun getDataLoadingSubject(): DataLoadingSubject {
        return dataManager
    }
}
