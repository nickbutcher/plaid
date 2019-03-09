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
import io.plaidapp.core.feed.FeedProgressUiModel
import io.plaidapp.core.feed.FeedUiModel
import io.plaidapp.core.util.event.Event
import io.plaidapp.search.domain.SearchDataManager

/**
 * [ViewModel] for the [SearchActivity]. Works with the data manager to load data and prepares it
 * for display in the [SearchActivity].
 */
class SearchViewModel(private val dataManager: SearchDataManager) : ViewModel() {

    private val _searchResults = MutableLiveData<Event<FeedUiModel>>()
    val searchResults: LiveData<Event<FeedUiModel>>
        get() = _searchResults

    private val _searchProgress = MutableLiveData<FeedProgressUiModel>()
    val searchProgress: LiveData<FeedProgressUiModel>
        get() = _searchProgress

    private val onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>> =
        object : OnDataLoadedCallback<List<PlaidItem>> {
            override fun onDataLoaded(data: List<PlaidItem>) {
                _searchResults.value = Event(FeedUiModel(data))
            }
        }

    private val dataLoadingCallbacks = object : DataLoadingSubject.DataLoadingCallbacks {
        override fun dataStartedLoading() {
            _searchProgress.value = FeedProgressUiModel(true)
        }

        override fun dataFinishedLoading() {
            _searchProgress.value = FeedProgressUiModel(false)
        }
    }

    init {
        dataManager.onDataLoadedCallback = onDataLoadedCallback
        dataManager.registerCallback(dataLoadingCallbacks)
    }

    fun searchFor(query: String) {
        dataManager.searchFor(query)
    }

    fun loadMore() {
        dataManager.loadMore()
    }

    override fun onCleared() {
        dataManager.cancelLoading()
        super.onCleared()
    }

    fun clearResults() {
        dataManager.clear()
    }

    fun getDataLoadingSubject(): DataLoadingSubject {
        return dataManager
    }
}
