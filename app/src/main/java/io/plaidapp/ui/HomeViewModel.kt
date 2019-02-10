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

package io.plaidapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.plaidapp.core.data.DataManager
import io.plaidapp.core.data.Source
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import io.plaidapp.core.ui.filter.SourceUiModel
import io.plaidapp.core.ui.filter.SourcesHighlightUiModel
import io.plaidapp.core.ui.filter.SourcesUiModel
import io.plaidapp.core.util.event.Event
import java.util.Collections

class HomeViewModel(
    val dataManager: DataManager,
    private val loginRepository: LoginRepository,
    private val sourcesRepository: SourcesRepository
) : ViewModel() {

    // TODO keeping this one temporarily, until we deal with [FeedAdapter]
    private val _sourceRemoved = MutableLiveData<Source>()
    val sourceRemoved: LiveData<Source>
        get() = _sourceRemoved

    private val _sources = MutableLiveData<SourcesUiModel>()
    val sources: LiveData<SourcesUiModel>
        get() = _sources

    // listener for notifying adapter when data sources are deactivated
    private val filtersChangedCallbacks = object : FiltersChangedCallback() {
        override fun onFiltersChanged(changedFilter: Source) {
            if (!changedFilter.active) {
                _sourceRemoved.value = changedFilter
            }
        }

        override fun onFilterRemoved(removed: Source) {
            _sourceRemoved.value = removed
        }

        override fun onFiltersUpdated(sources: List<Source>) {
            updateSourcesUiModel(sources)
        }
    }

    init {
        sourcesRepository.registerFilterChangedCallback(filtersChangedCallbacks)
        getSources()
    }

    fun isDesignerNewsUserLoggedIn() = loginRepository.isLoggedIn

    fun logoutFromDesignerNews() {
        loginRepository.logout()
    }

    fun loadData() {
        dataManager.loadAllDataSources()
    }

    override fun onCleared() {
        dataManager.cancelLoading()
        super.onCleared()
    }

    fun addSources(query: String, isDribbble: Boolean, isDesignerNews: Boolean) {
        val sources = mutableListOf<Source>()
        if (isDribbble) {
            sources.add(Source.DribbbleSearchSource(query, true))
        }
        if (isDesignerNews) {
            sources.add(Source.DesignerNewsSearchSource(query, true))
        }
        sourcesRepository.addOrMarkActiveSources(sources)
    }

    private fun getSources() {
        updateSourcesUiModel(sourcesRepository.getSources())
    }

    private fun updateSourcesUiModel(sources: List<Source>) {
        val newSourcesUiModel = createNewSourceUiModels(sources)
        val oldSourceUiModel = _sources.value
        if (oldSourceUiModel == null) {
            _sources.value = SourcesUiModel(newSourcesUiModel)
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
            _sources.value = SourcesUiModel(newSourcesUiModel, event)
        }
    }

    private fun createSourcesHighlightUiModel(
        oldSources: List<SourceUiModel>,
        newSources: List<SourceUiModel>
    ): SourcesHighlightUiModel? {
        // if something was just updated or removed but not added, there's nothing to highlight
        if (oldSources.size >= newSources.count()) {
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

    private fun createNewSourceUiModels(sources: List<Source>): List<SourceUiModel> {
        val mutableSources = sources.toMutableList()
        Collections.sort(mutableSources, Source.SourceComparator())
        return mutableSources.map {
            SourceUiModel(
                    it.key,
                    it.name,
                    it.active,
                    it.iconRes,
                    it.isSwipeDismissable,
                    { source -> sourcesRepository.changeSourceActiveState(source.key, !source.active) },
                    { source ->
                        if (source.isSwipeDismissable) {
                            sourcesRepository.removeSource(source.key)
                        }
                    }
            )
        }
    }
}
