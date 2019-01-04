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

import io.plaidapp.core.data.Source
import java.util.Collections

/**
 * Manage saving and retrieving data sources from disk.
 */
class SourcesRepository(
    private val defaultSources: List<Source>,
    private val dataSource: SourcesLocalDataSource
) {

    fun getSources(): List<Source> {
        val sourceKeys = dataSource.getSources()
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
        return sources
    }

    private fun addSources(sources: List<Source>) {
        val sourceKeys = sources.map { it.key }.toSet()
        dataSource.addSources(sourceKeys, true)
    }

    fun addSource(toAdd: Source) {
        dataSource.addSource(toAdd.key, toAdd.active)
    }

    fun updateSource(source: Source) {
        dataSource.updateSource(source.key, source.active)
    }

    fun removeSource(source: Source) {
        dataSource.removeSource(source.key)
    }

    private fun getSourceFromDefaults(key: String, active: Boolean): Source? {
        return defaultSources.firstOrNull { source -> source.key == key }
                .also { it?.active = active }
    }

    companion object {
        const val SOURCE_DESIGNER_NEWS_POPULAR = "SOURCE_DESIGNER_NEWS_POPULAR"
        const val SOURCE_PRODUCT_HUNT = "SOURCE_PRODUCT_HUNT"
    }
}
