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

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.SourceItem
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.dribbble.data.DribbbleSourceItem.Companion.DRIBBBLE_QUERY_PREFIX
import io.plaidapp.core.producthunt.data.ProductHuntSourceItem
import io.plaidapp.core.producthunt.data.ProductHuntSourceItem.Companion.SOURCE_PRODUCT_HUNT
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import io.plaidapp.test.shared.provideFakeCoroutinesDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [SourcesRepository] mocking the dependencies.
 */
@ExperimentalCoroutinesApi
class SourcesRepositoryTest {

    private val dnSourceKey = "DESIGNER_NEWS_QUERY_query"
    private val designerNewsSource = DribbbleSourceItem(
        "query",
        true
    )
    private val dribbbleSourceKey = DRIBBBLE_QUERY_PREFIX + "dribbble"
    private val dribbbleSource = DribbbleSourceItem("dribbble", true)
    private val productHuntSource = ProductHuntSourceItem("product hunt")
    private val defaultSources = listOf(designerNewsSource, dribbbleSource, productHuntSource)
    private val defaultSourcesKeys = setOf(dnSourceKey, dribbbleSourceKey, SOURCE_PRODUCT_HUNT)

    private val localDataSource: SourcesLocalDataSource = mock()
    private val repository = SourcesRepository(
        defaultSources,
        localDataSource,
        provideFakeCoroutinesDispatcherProvider()
    )

    @Test
    fun getSources_whenNoOtherSourceWasAdded() = runBlocking {
        // Given that no other source was added
        whenever(localDataSource.getKeys()).thenReturn(null)

        // When getting the sources
        val sources = repository.getSources()

        // Then the default sources are returned
        assertEquals(defaultSources, sources)
    }

    @Test
    fun getSources_whenOtherSourcesWereAdded() = runBlocking {
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
    fun getSources_whenDeprecatedSourcesWereAdded() = runBlocking {
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
        // Given that an active source is added
        repository.addSources(listOf(designerNewsSource))

        // When changing the active state of a source
        repository.changeSourceActiveState(designerNewsSource.key)

        // Then the source was updated in the data source
        verify(localDataSource).updateSource(designerNewsSource.key, false)
    }

    @Test
    fun removeSource() {
        // When removing a source
        repository.removeSource("key")

        // Then the source was removed from the data source
        verify(localDataSource).removeSource("key")
    }

    @Test
    fun addSources_addsSourceCache() = runBlocking {
        // When adding a source
        repository.addSources(listOf(designerNewsSource))

        // Then the source is returned
        val sources = repository.getSources()
        assertEquals(listOf(designerNewsSource), sources)
    }

    @Test
    fun changeSourceActiveState_updatesInCache() = runBlocking {
        // Given an added source
        repository.addSources(listOf(designerNewsSource))

        // When changing the active state of a source
        repository.changeSourceActiveState(designerNewsSource.key)

        // Then the updated source is returned
        val sources = repository.getSources()
        assertEquals(1, sources.size)
        val updatedSource = sources[0]
        assertEquals(designerNewsSource.key, updatedSource.key)
        assertEquals(false, updatedSource.active)
    }

    @Test
    fun removeSource_removesFromCache() = runBlocking {
        // Given an added source
        repository.addSources(listOf(designerNewsSource))

        // When removing a source
        repository.removeSource(designerNewsSource.key)

        // Then the source was removed from cache
        val sources = repository.getSources()
        assertTrue(sources.isEmpty())
    }

    @Test
    fun listenerNotified_whenSourceAdded() {
        // Given a callback registered
        var sourceAdded: List<SourceItem>? = null
        val callback = object : FiltersChangedCallback() {
            override fun onFiltersUpdated(sources: List<SourceItem>) {
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
        // Given a source added
        repository.addSources(listOf(designerNewsSource))
        // Given a callback registered
        var sourceUpdated: SourceItem? = null
        val callback = object : FiltersChangedCallback() {
            override fun onFiltersChanged(changedFilter: SourceItem) {
                super.onFiltersChanged(changedFilter)
                sourceUpdated = changedFilter
            }
        }
        repository.registerFilterChangedCallback(callback)

        // When changing the active state of a source
        repository.changeSourceActiveState(designerNewsSource.key)

        // Then the callback was triggered
        assertEquals(sourceUpdated, designerNewsSource)
    }

    @Test
    fun listenerNotified_whenSourceRemoved() {
        // Given a source added
        repository.addSources(listOf(designerNewsSource))
        // Given a callback registered
        var sourceRemoved: String? = null
        val callback = object : FiltersChangedCallback() {
            override fun onFilterRemoved(sourceKey: String) {
                sourceRemoved = sourceKey
            }
        }
        repository.registerFilterChangedCallback(callback)

        // When removing a source
        repository.removeSource(designerNewsSource.key)

        // Then the callback was triggered
        assertEquals(sourceRemoved, designerNewsSource.key)
    }

    @Test
    fun getActiveSourceCount() {
        // Given an active and an inactive source added
        repository.addSources(listOf(designerNewsSource, productHuntSource))
        val keys = setOf(dnSourceKey, SOURCE_PRODUCT_HUNT)
        whenever(localDataSource.getKeys()).thenReturn(keys)
        whenever(localDataSource.getSourceActiveState(dnSourceKey)).thenReturn(true)

        // When getting the number of active sources
        val activeSources = repository.getActiveSourcesCount()

        // Then the correct number is returned
        assertEquals(1, activeSources)
    }
}
