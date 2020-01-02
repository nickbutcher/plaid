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

package io.plaidapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.data.DataManager
import io.plaidapp.core.data.OnDataLoadedCallback
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.SourceItem
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.feed.FeedProgressUiModel
import io.plaidapp.core.feed.FeedUiModel
import io.plaidapp.core.ui.expandPopularItems
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import io.plaidapp.core.ui.filter.SourceUiModel
import io.plaidapp.core.ui.filter.SourcesHighlightUiModel
import io.plaidapp.core.ui.filter.SourcesUiModel
import io.plaidapp.core.ui.getPlaidItemsForDisplay
import io.plaidapp.core.util.event.Event
import java.util.Collections
import kotlinx.coroutines.launch

/**
 * ViewModel for [HomeActivity].
 * Handles user login status and sources display for the drawer.
 *
 * TODO: handling of the feed list
 */
class HomeViewModel(
    val dataManager: DataManager,
    private val designerNewsLoginRepository: LoginRepository,
    private val sourcesRepository: SourcesRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    private val _sources = MutableLiveData<SourcesUiModel>()
    val sources: LiveData<SourcesUiModel>
        get() = _sources

    private val _feedProgress = MutableLiveData<FeedProgressUiModel>()
    val feedProgress: LiveData<FeedProgressUiModel>
        get() = _feedProgress

    private val feedData = MutableLiveData<List<PlaidItem>>()

    private val onDataLoadedCallback = object : OnDataLoadedCallback<List<PlaidItem>> {
        override fun onDataLoaded(data: List<PlaidItem>) {
            val oldItems = feedData.value.orEmpty()
            updateFeedData(oldItems, data)
        }
    }
    // listener for notifying adapter when data sources are deactivated
    private val filtersChangedCallbacks = object : FiltersChangedCallback() {
        override fun onFiltersChanged(changedFilter: SourceItem) {
            if (!changedFilter.active) {
                handleDataSourceRemoved(changedFilter.key, feedData.value.orEmpty())
            }
        }

        override fun onFilterRemoved(sourceKey: String) {
            handleDataSourceRemoved(sourceKey, feedData.value.orEmpty())
        }

        override fun onFiltersUpdated(sources: List<SourceItem>) {
            updateSourcesUiModel(sources)
        }
    }

    private val dataLoadingCallbacks = object : DataLoadingSubject.DataLoadingCallbacks {
        override fun dataStartedLoading() {
            _feedProgress.postValue(FeedProgressUiModel(true))
        }

        override fun dataFinishedLoading() {
            _feedProgress.postValue(FeedProgressUiModel(false))
        }
    }

    init {
        sourcesRepository.registerFilterChangedCallback(filtersChangedCallbacks)
        dataManager.setOnDataLoadedCallback(onDataLoadedCallback)
        dataManager.registerCallback(dataLoadingCallbacks)
        getSources()
        loadData()
    }

    fun getFeed(columns: Int) = feedData.switchMap {
        liveData(viewModelScope.coroutineContext + dispatcherProvider.computation) {
            expandPopularItems(it, columns)
            emit(FeedUiModel(it))
        }
    }

    fun isDesignerNewsUserLoggedIn() = designerNewsLoginRepository.isLoggedIn

    fun logoutFromDesignerNews() {
        designerNewsLoginRepository.logout()
    }

    fun loadData() = viewModelScope.launch {
        dataManager.loadMore()
    }

    override fun onCleared() {
        dataManager.cancelLoading()
        super.onCleared()
    }

    fun addSources(query: String, isDribbble: Boolean, isDesignerNews: Boolean) {
        if (query.isBlank()) {
            return
        }
        val sources = mutableListOf<SourceItem>()
        if (isDribbble) {
            sources.add(DribbbleSourceItem(query, true))
        }
        if (isDesignerNews) {
            sources.add(DesignerNewsSearchSourceItem(query, true))
        }
        viewModelScope.launch(dispatcherProvider.io) {
            sourcesRepository.addOrMarkActiveSources(sources)
        }
    }

    private fun getSources() {
        viewModelScope.launch(dispatcherProvider.io) {
            val sources = sourcesRepository.getSources()
            updateSourcesUiModel(sources)
        }
    }

    private fun updateSourcesUiModel(sources: List<SourceItem>) {
        val newSourcesUiModel = createNewSourceUiModels(sources)
        val oldSourceUiModel = _sources.value
        if (oldSourceUiModel == null) {
            _sources.postValue(SourcesUiModel(newSourcesUiModel))
        } else {
            val highlightUiModel = createSourcesHighlightUiModel(
                oldSourceUiModel.sourceUiModels,
                newSourcesUiModel
            )
            val event = if (highlightUiModel != null) {
                Event(highlightUiModel)
            } else {
                null
            }
            _sources.postValue(SourcesUiModel(newSourcesUiModel, event))
        }
    }

    private fun createSourcesHighlightUiModel(
        oldSources: List<SourceUiModel>,
        newSources: List<SourceUiModel>
    ): SourcesHighlightUiModel? {
        // if something was just updated or removed but not added, there's nothing to highlight
        if (oldSources.size >= newSources.size) {
            return null
        }
        // something was added. Find out what
        val positions = mutableListOf<Int>()
        var itemsAdded = 0

        for (i in 0 until oldSources.count()) {
            val item = oldSources[i]
            if (item.key != newSources[i + itemsAdded].key) {
                // we have a new item
                positions.add(i + itemsAdded)
                itemsAdded++
            }
        }
        val lastItems = (oldSources.count() + itemsAdded)
        for (i in lastItems until newSources.size) {
            positions.add(i)
        }

        val scrollToPosition = positions.max()
        return if (scrollToPosition == null) {
            null
        } else {
            SourcesHighlightUiModel(positions, scrollToPosition)
        }
    }

    private fun updateFeedData(oldItems: List<PlaidItem>, newItems: List<PlaidItem>) {
        feedData.postValue(getPlaidItemsForDisplay(oldItems, newItems))
    }

    private fun handleDataSourceRemoved(dataSourceKey: String, oldItems: List<PlaidItem>) {
        val items = oldItems.toMutableList()
        items.removeAll {
            dataSourceKey == it.dataSource
        }
        feedData.postValue(items)
    }

    private fun createNewSourceUiModels(sources: List<SourceItem>): List<SourceUiModel> {
        val mutableSources = sources.toMutableList()
        Collections.sort(mutableSources, SourceItem.SourceComparator())
        return mutableSources.map {
            SourceUiModel(
                it.id,
                it.key,
                it.name,
                it.active,
                it.iconRes,
                it.isSwipeDismissable,
                { sourceUiModel -> sourcesRepository.changeSourceActiveState(sourceUiModel.key) },
                { sourceUiModel ->
                    if (sourceUiModel.isSwipeDismissable) {
                        sourcesRepository.removeSource(sourceUiModel.key)
                    }
                }
            )
        }
    }
}
