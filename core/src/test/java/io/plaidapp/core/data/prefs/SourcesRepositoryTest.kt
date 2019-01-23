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

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.R
import io.plaidapp.core.data.Source
import io.plaidapp.core.data.Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [SourcesRepository] mocking the dependencies.
 */
class SourcesRepositoryTest {

    private val dnSourceKey = "DESIGNER_NEWS_QUERY_query"
    private val designerNewsSource = Source.DesignerNewsSearchSource(
            "query",
            true
    )
    private val dribbbleSourceKey = DRIBBBLE_QUERY_PREFIX + "dribbble"
    private val dribbbleSource = Source.DribbbleSearchSource("dribbble", true)
    private val phSourceKey = "PH"
    private val productHuntSource = Source(
            phSourceKey,
            500,
            "product hung",
            R.drawable.ic_product_hunt,
            false)
    private val defaultSources = listOf(designerNewsSource, dribbbleSource, productHuntSource)
    private val defaultSourcesKeys = setOf(dnSourceKey, dribbbleSourceKey, phSourceKey)

    private val localDataSource: SourcesLocalDataSource = mock()
    private val repository = SourcesRepository(defaultSources, localDataSource)

    @Test
    fun getSources_whenNoOtherSourceWasAdded() {
        // Given that no other source was added
        whenever(localDataSource.getKeys()).thenReturn(null)

        // When getting the sources
        val sources = repository.getSources()

        // Then the default sources are returned
        assertEquals(defaultSources, sources)
    }

    @Test
    fun getSources_whenOtherSourcesWereAdded() {
        // Given that other sources were added
        whenever(localDataSource.getKeys()).thenReturn(defaultSourcesKeys)
        whenever(localDataSource.getSourceActiveState(eq(dribbbleSource.key)))
                .thenReturn(dribbbleSource.active)

        // When getting the sources
        val sources = repository.getSources()

        // Then the default sources are returned
        assertEquals(defaultSources.size, sources.size)
        // TODO the sources are not yet data classes, so they can't be compared
        // with ==. Since Dribbble and Designer News sources are recreated, then
        // the would need to be compared field by field
        // Test to be updated once the sources are data classes
    }

    @Test
    fun getSources_whenDeprecatedSourcesWereAdded() {
        // Given that other deprecated sources were added
        val oldSources = mutableSetOf(
                "SOURCE_DESIGNER_NEWS_RECENT",
                "SOURCE_DRIBBBLE_query"
        ).toSet()
        whenever(localDataSource.getKeys()).thenReturn(oldSources)

        // When getting the sources
        val sources = repository.getSources()

        // Then the list of sources is empty
        assertTrue(sources.isEmpty())
    }

    @Test
    fun addSources_addsSourcesToDataSource() {
        // When adding a list of sources
        repository.addSources(listOf(designerNewsSource))

        // Then the source was added to the data source
        verify(localDataSource).addSource(designerNewsSource.key, designerNewsSource.active)
    }

    @Test
    fun changeSourceActiveState() {
        // When changing the active state of a source
        repository.changeSourceActiveState(designerNewsSource)

        // Then the source was updated in the data source
        verify(localDataSource).updateSource(designerNewsSource.key, !designerNewsSource.active)
    }

    @Test
    fun removeSource() {
        // When removing a source
        repository.removeSource(designerNewsSource)

        // Then the source was removed from the data source
        verify(localDataSource).removeSource(designerNewsSource.key)
    }

    @Test
    fun addSources_addsSourceCache() {
        // When adding a source
        repository.addSources(listOf(designerNewsSource))

        // Then the source is returned
        val sources = repository.getSources()
        assertEquals(listOf(designerNewsSource), sources)
    }

    @Test
    fun changeSourceActiveState_updatesInCache() {
        // Given an added source
        repository.addSources(listOf(designerNewsSource))

        // When changing the active state of a source
        val designerNewsInactive = Source.DesignerNewsSearchSource(
                "query",
                false
        )
        repository.changeSourceActiveState(designerNewsInactive)

        // Then the updated source is returned
        val sources = repository.getSources()
        assertEquals(1, sources.size)
        val updatedSource = sources[0]
        assertEquals(designerNewsInactive.key, updatedSource.key)
        assertEquals(!designerNewsInactive.active, updatedSource.active)
    }

    @Test
    fun removeSource_removesFromCache() {
        // Given an added source
        repository.addSources(listOf(designerNewsSource))

        // When removing a source
        repository.removeSource(designerNewsSource)

        // Then the source was removed from cache
        val sources = repository.getSources()
        assertTrue(sources.isEmpty())
    }

    @Test
    fun listenerNotified_whenSourceAdded() {
        // Given a callback registered
        var sourceAdded: List<Source>? = null
        val callback = object : FiltersChangedCallback() {
            override fun onFiltersUpdated(sources: List<Source>) {
                super.onFiltersUpdated(sources)
                sourceAdded = sources
            }
        }
        repository.registerFilterChangedCallback(callback)

        // When adding a list of sources
        repository.addSources(listOf(designerNewsSource))

        // Then the callback was triggered
        assertEquals(sourceAdded, listOf(designerNewsSource))
    }

    @Test
    fun listenerNotified_whenSourceActiveStateChanged() {
        // Given a callback registered
        var sourceUpdated: Source? = null
        val callback = object : FiltersChangedCallback() {
            override fun onFiltersChanged(changedFilter: Source) {
                super.onFiltersChanged(changedFilter)
                sourceUpdated = changedFilter
            }
        }
        repository.registerFilterChangedCallback(callback)

        // When changing the active state of a source
        repository.changeSourceActiveState(designerNewsSource)

        // Then the callback was triggered
        assertEquals(sourceUpdated, designerNewsSource)
    }

    @Test
    fun listenerNotified_whenSourceRemoved() {
        // Given a callback registered
        var sourceRemoved: Source? = null
        val callback = object : FiltersChangedCallback() {
            override fun onFilterRemoved(removed: Source) {
                sourceRemoved = removed
            }
        }
        repository.registerFilterChangedCallback(callback)

        // When removing a source
        repository.removeSource(designerNewsSource)

        // Then the callback was triggered
        assertEquals(sourceRemoved, designerNewsSource)
    }

    @Test
    fun getActiveSourceCount() {
        // Given an active and an inactive source added
        repository.addSources(listOf(designerNewsSource, productHuntSource))
        val keys = setOf(dnSourceKey, phSourceKey)
        whenever(localDataSource.getKeys()).thenReturn(keys)
        whenever(localDataSource.getSourceActiveState(dnSourceKey)).thenReturn(true)

        // When getting the number of active sources
        val activeSources = repository.getActiveSourcesCount()

        // Then the correct number is returned
        assertEquals(1, activeSources)
    }
}
