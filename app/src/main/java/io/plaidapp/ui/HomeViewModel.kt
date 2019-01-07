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
import io.plaidapp.core.ui.filter.SourceUiModel
import io.plaidapp.core.ui.filter.FiltersChangedCallback
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

    private val _sources = MutableLiveData<List<SourceUiModel>>()
    val sources: LiveData<List<SourceUiModel>>
        get() = _sources

    // listener for notifying adapter when data sources are deactivated
    private val filtersChangedCallbacks = object : FiltersChangedCallback() {
        override fun onFiltersChanged(changedFilter: Source) {
            updateSources()
            if (!changedFilter.active) {
                _sourceRemoved.value = changedFilter
            }
        }

        override fun onFilterRemoved(removed: Source) {
            updateSources()
            _sourceRemoved.value = removed
        }
    }

    init {
        updateSources()
        sourcesRepository.registerFilterChangedCallback(filtersChangedCallbacks)
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

    /**
     * Adds the source and returns true if it's a new source
     */
    fun addSource(toAdd: Source): Boolean {
        val filters = sources.value ?: return false

        // first check if it already exists
        for (i in 0 until filters.size) {
            val existing = filters[i]
            if (existing.source.javaClass == toAdd.javaClass &&
                    existing.source.key.equals(toAdd.key, ignoreCase = true)
            ) {
                // already exists, just ensure it's active
                if (!existing.source.active) {
                    sourcesRepository.changeSourceActiveState(existing.source)
                }
                return false
            }
        }
        // didn't already exist, so add it
        sourcesRepository.addSource(toAdd)
        updateSources()
        return true
    }

    private fun updateSources() {
        val mutableSources = sourcesRepository.getSources()
        Collections.sort(mutableSources, Source.SourceComparator())
        _sources.value = mutableSources.map {
            SourceUiModel(
                    it,
                    { source -> sourcesRepository.changeSourceActiveState(source) },
                    { source ->
                        if (source.isSwipeDismissable) {
                            sourcesRepository.removeSource(source)
                        }
                    }
            )
        }
    }
}
