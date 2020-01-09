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

package io.plaidapp.core.data.prefs

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.SourceItem
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import java.util.Collections
import kotlinx.coroutines.withContext

/**
 * Manage saving and retrieving data sources from disk.
 */
class SourcesRepository(
    private val defaultSources: List<SourceItem>,
    private val dataSource: SourcesLocalDataSource,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {

    private val cache = mutableListOf<SourceItem>()
    private val callbacks = mutableListOf<FiltersChangedCallback>()

    fun registerFilterChangedCallback(callback: FiltersChangedCallback) {
        callbacks.add(callback)
    }

    suspend fun getSources(): List<SourceItem> = withContext(dispatcherProvider.io) {
        return@withContext getSourcesSync()
    }

    @Deprecated("Use the suspending getSources")
    fun getSourcesSync(): List<SourceItem> {
        if (cache.isNotEmpty()) {
            return cache
        }
        // cache is empty
        val sourceKeys = dataSource.getKeys()
        if (sourceKeys == null) {
            addSources(defaultSources)
            return defaultSources
        }

        val sources = mutableListOf<SourceItem>()
        sourceKeys.forEach { sourceKey ->
            val activeState = dataSource.getSourceActiveState(sourceKey)
            when {
                // add Dribbble source
                sourceKey.startsWith(DribbbleSourceItem.DRIBBBLE_QUERY_PREFIX) -> {
                    val query = sourceKey.replace(DribbbleSourceItem.DRIBBBLE_QUERY_PREFIX, "")
                    sources.add(DribbbleSourceItem(query, activeState))
                }
                // add Designer News source
                sourceKey.startsWith(DesignerNewsSearchSourceItem.DESIGNER_NEWS_QUERY_PREFIX) -> {
                    val query = sourceKey.replace(DesignerNewsSearchSourceItem
                            .DESIGNER_NEWS_QUERY_PREFIX, "")
                    sources.add(DesignerNewsSearchSourceItem(query, activeState))
                }
                // remove deprecated sources
                isDeprecatedDesignerNewsSource(sourceKey) -> dataSource.removeSource(sourceKey)
                isDeprecatedDribbbleV1Source(sourceKey) -> dataSource.removeSource(sourceKey)
                else -> getSourceFromDefaults(sourceKey, activeState)?.let { sources.add(it) }
            }
        }
        Collections.sort(sources, SourceItem.SourceComparator())
        cache.addAll(sources)
        dispatchSourcesUpdated()
        return cache
    }

    fun addSources(sources: List<SourceItem>) {
        sources.forEach { dataSource.addSource(it.key, it.active) }
        cache.addAll(sources)
        dispatchSourcesUpdated()
    }

    fun addOrMarkActiveSources(sources: List<SourceItem>) {
        val sourcesToAdd = mutableListOf<SourceItem>()
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
                        changeSourceActiveState(existing.key)
                    }
                }
            }
            if (!sourcePresent) {
                // doesn't exist so needs to be added
                sourcesToAdd += toAdd
            }
        }
        // they didn't already exist, so add them
        addSources(sourcesToAdd)
    }

    fun changeSourceActiveState(sourceKey: String) {
        cache.find { it.key == sourceKey }?.let {
            val newActiveState = !it.active
            it.active = newActiveState
            dataSource.updateSource(sourceKey, newActiveState)
            dispatchSourceChanged(it)
        }
        dispatchSourcesUpdated()
    }

    fun removeSource(sourceKey: String) {
        dataSource.removeSource(sourceKey)
        cache.removeAll { it.key == sourceKey }
        dispatchSourceRemoved(sourceKey)
        dispatchSourcesUpdated()
    }

    fun getActiveSourcesCount(): Int {
        return getSourcesSync().count { it.active }
    }

    private fun getSourceFromDefaults(key: String, active: Boolean): SourceItem? {
        return defaultSources.firstOrNull { source -> source.key == key }
                .also { it?.active = active }
    }

    private fun dispatchSourcesUpdated() {
        callbacks.forEach { it.onFiltersUpdated(cache) }
    }

    private fun dispatchSourceChanged(source: SourceItem) {
        callbacks.forEach { it.onFiltersChanged(source) }
    }

    private fun dispatchSourceRemoved(sourceKey: String) {
        callbacks.forEach { it.onFilterRemoved(sourceKey) }
    }

    companion object {

        @Volatile
        private var INSTANCE: SourcesRepository? = null

        fun getInstance(
            defaultSources: List<SourceItem>,
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
