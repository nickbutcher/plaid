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

import android.content.Context
import io.plaidapp.core.R
import io.plaidapp.core.data.Source
import java.util.Collections
import javax.inject.Inject

/**
 * Manage saving and retrieving data sources from disk.
 */
class SourcesRepository @Inject constructor(
    context: Context,
    private val dataSource: SourcesLocalDataSource
) {

    private val defaultDesignerNewsSourceName = context.getString(R.string.source_designer_news_popular)
    private val defaultDribbbleSourceName = context.getString(R.string
            .source_dribbble_search_material_design)
    private val defaultProductHuntSourceName = context.getString(R.string.source_product_hunt)

    fun getSources(): List<Source> {
        val sourceKeys = dataSource.getSources()
        if (sourceKeys == null) {
            val sources = getDefaultSources()
            addSources(sources)
            return sources.toList()
        }

        val sources = mutableSetOf<Source>()
        for (sourceKey in sourceKeys) {
            val activeState = dataSource.getSourceActiveState(sourceKey)
            when {
                sourceKey.startsWith(
                        Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX) -> sources.add(
                        Source.DribbbleSearchSource(
                                sourceKey.replace(Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX, ""),
                                activeState
                        )
                )
                isDeprecatedDesignerNewsSource(sourceKey) -> dataSource.removeSource(sourceKey)
                sourceKey.startsWith(Source.DesignerNewsSearchSource
                        .DESIGNER_NEWS_QUERY_PREFIX) -> sources.add(
                        Source.DesignerNewsSearchSource(
                                sourceKey.replace(Source.DesignerNewsSearchSource
                                        .DESIGNER_NEWS_QUERY_PREFIX, ""),
                                activeState
                        )
                )
                isDeprecatedDribbbleV1Source(sourceKey) -> dataSource.removeSource(sourceKey)
                else -> // TODO improve this O(n2) search
                    getSourceFromDefaults(sourceKey, activeState)?.let { sources.add(it) }
            }
        }
        val sourcesList = sources.toList()
        Collections.sort(sourcesList, Source.SourceComparator())
        return sourcesList
    }

    private fun addSources(sources: Set<Source>) {
        val sourceKeys = sources.map { it.key }.toSet()
        dataSource.addSources(sourceKeys, true)
    }

    private fun getDefaultSources(): Set<Source> {
        val defaultSources = mutableSetOf<Source>()
        defaultSources.add(Source.DesignerNewsSource(SOURCE_DESIGNER_NEWS_POPULAR, 100,
                defaultDesignerNewsSourceName, true))
        // 200 sort order range left for DN searches
        defaultSources.add(Source.DribbbleSearchSource(defaultDribbbleSourceName, true))
        // 400 sort order range left for dribbble searches
        defaultSources.add(Source(SOURCE_PRODUCT_HUNT, 500,
                defaultProductHuntSourceName, R.drawable.ic_product_hunt, false))
        return defaultSources
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
        for (source in getDefaultSources()) {
            if (source.key == key) {
                source.active = active
                return source
            }
        }
        return null
    }

    companion object {

        const val SOURCE_DESIGNER_NEWS_POPULAR = "SOURCE_DESIGNER_NEWS_POPULAR"
        const val SOURCE_PRODUCT_HUNT = "SOURCE_PRODUCT_HUNT"
        private val SOURCES_PREF = "SOURCES_PREF"
        private val KEY_SOURCES = "KEY_SOURCES"
    }
}
