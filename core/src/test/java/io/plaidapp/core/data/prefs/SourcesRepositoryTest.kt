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
    fun addSource_addsSourceToDataSource() {
        // When adding a source
        repository.addSource(designerNewsSource)

        // Then the source was added to the data source
        verify(localDataSource).addSource(designerNewsSource.key, designerNewsSource.active)
    }

    @Test
    fun updateSource() {
        // When updating a source
        repository.updateSource(designerNewsSource)

        // Then the source was updated in the data source
        verify(localDataSource).updateSource(designerNewsSource.key, designerNewsSource.active)
    }

    @Test
    fun removeSource() {
        // When removing a source
        repository.removeSource(designerNewsSource)

        // Then the source was removed from the data source
        verify(localDataSource).removeSource(designerNewsSource.key)
    }
}
