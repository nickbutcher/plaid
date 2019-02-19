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

package io.plaidapp.core.data.prefs

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.Source
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import kotlinx.coroutines.withContext
import java.util.Collections

/**
 * Manage saving and retrieving data sources from disk.
 */
class SourcesRepository(
    private val defaultSources: List<Source>,
    private val dataSource: SourcesLocalDataSource,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {

    private val cache = mutableListOf<Source>()
    private val callbacks = mutableListOf<FiltersChangedCallback>()

    fun registerFilterChangedCallback(callback: FiltersChangedCallback) {
        callbacks.add(callback)
    }

    suspend fun getSources() = withContext(dispatcherProvider.io) {
        getSourcesSync()
    }

    @Deprecated("Use the suspending getSourcesSync")
    fun getSourcesSync(): List<Source> {
        if (cache.isNotEmpty()) {
            return cache
        }
        // cache is empty
        val sourceKeys = dataSource.getKeys()
        if (sourceKeys == null) {
            addSources(defaultSources)
            return defaultSources
        }

        val sources = mutableListOf<Source>()
        sourceKeys.forEach { sourceKey ->
            val activeState = dataSource.getSourceActiveState(sourceKey)
            when {
                // add Dribbble source
                sourceKey.startsWith(Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX) -> {
                    val query = sourceKey.replace(Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX, "")
                    sources.add(Source.DribbbleSearchSource(query, activeState))
                }
                // add Designer News source
                sourceKey.startsWith(Source.DesignerNewsSearchSource.DESIGNER_NEWS_QUERY_PREFIX) -> {
                    val query = sourceKey.replace(Source.DesignerNewsSearchSource
                            .DESIGNER_NEWS_QUERY_PREFIX, "")
                    sources.add(Source.DesignerNewsSearchSource(query, activeState))
                }
                // remove deprecated sources
                isDeprecatedDesignerNewsSource(sourceKey) -> dataSource.removeSource(sourceKey)
                isDeprecatedDribbbleV1Source(sourceKey) -> dataSource.removeSource(sourceKey)
                else -> getSourceFromDefaults(sourceKey, activeState)?.let { sources.add(it) }
            }
        }
        Collections.sort(sources, Source.SourceComparator())
        cache.addAll(sources)
        dispatchSourcesUpdated()
        return cache
    }

    fun addSources(sources: List<Source>) {
        sources.forEach { dataSource.addSource(it.key, it.active) }
        cache.addAll(sources)
        dispatchSourcesUpdated()
    }

    fun addOrMarkActiveSources(sources: List<Source>) {
        val sourcesToAdd = mutableListOf<Source>()
        sources.forEach { toAdd ->
            // first check if it already exists
            var sourcePresent = false
            for (i in 0 until cache.size) {
                val existing = cache[i]
                if (existing.javaClass == toAdd.javaClass &&
                        existing.key.equals(toAdd.key, ignoreCase = true)
                ) {
                    sourcePresent = true
                    // already exists, just ensure it's active
                    if (!existing.active) {
                        changeSourceActiveState(existing.key, true)
                    }
                }
            }
            if (!sourcePresent) {
                // doesn't exist so needs to be added
                sourcesToAdd.add(toAdd)
            }
        }
        // they didn't already exist, so add them
        addSources(sourcesToAdd)
    }

    fun changeSourceActiveState(sourceKey: String, newActiveState: Boolean) {
        dataSource.updateSource(sourceKey, newActiveState)
        cache.find { it.key == sourceKey }?.apply {
            active = newActiveState
            dispatchSourceChanged(this)
        }
        dispatchSourcesUpdated()
    }

    fun removeSource(sourceKey: String) {
        dataSource.removeSource(sourceKey)
        cache.find { it.key == sourceKey }?.let {
            cache.remove(it)
            dispatchSourceRemoved(it)
        }
        dispatchSourcesUpdated()
    }

    fun getActiveSourcesCount(): Int {
        return getSourcesSync().count { it.active }
    }

    private fun getSourceFromDefaults(key: String, active: Boolean): Source? {
        return defaultSources.firstOrNull { source -> source.key == key }
                .also { it?.active = active }
    }

    private fun dispatchSourcesUpdated() {
        callbacks.forEach { it.onFiltersUpdated(cache) }
    }

    private fun dispatchSourceChanged(source: Source) {
        callbacks.forEach { it.onFiltersChanged(source) }
    }

    private fun dispatchSourceRemoved(source: Source) {
        callbacks.forEach { it.onFilterRemoved(source) }
    }

    companion object {
        const val SOURCE_DESIGNER_NEWS_POPULAR = "SOURCE_DESIGNER_NEWS_POPULAR"
        const val SOURCE_PRODUCT_HUNT = "SOURCE_PRODUCT_HUNT"

        @Volatile
        private var INSTANCE: SourcesRepository? = null

        fun getInstance(
            defaultSources: List<Source>,
            dataSource: SourcesLocalDataSource,
            dispatcherProvider: CoroutinesDispatcherProvider
        ): SourcesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SourcesRepository(defaultSources, dataSource, dispatcherProvider).also {
                    INSTANCE = it
                }
            }
        }
    }
}
