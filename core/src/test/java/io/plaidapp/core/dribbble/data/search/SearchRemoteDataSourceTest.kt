/*
 * Copyright 2018 Google, Inc.
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

package io.plaidapp.core.dribbble.data.search

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.errorResponseBody
import io.plaidapp.core.dribbble.data.search.SearchRemoteDataSource.SortOrder
import io.plaidapp.core.dribbble.data.shots
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests for [SearchRemoteDataSource] which mocks the search service dependency.
 */
class SearchRemoteDataSourceTest {

    private val service: DribbbleSearchService = mock()
    private val dataSource = SearchRemoteDataSource(service)

    private val query = "Plaid shirts"
    private val page = 0
    private val defaultSortOrder = DribbbleSearchService.SORT_RECENT
    private val defaultResultsPerPage = DribbbleSearchService.PER_PAGE_DEFAULT

    @Test
    fun search_whenRequestSuccessful() = runBlocking {
        // Given that the service responds with success
        withSuccess(shots)

        // When performing a search
        val response = dataSource.search(query, page, SortOrder.RECENT, defaultResultsPerPage)

        // Then the response is as expected
        assertNotNull(response)
        assertEquals(Result.Success(shots), response)
    }

    @Test
    fun search_whenRequestFailed() = runBlocking {
        // Given that the service responds with failure
        val result = Response.error<List<Shot>>(400, errorResponseBody)
        whenever(service.searchDeferred(query, page, defaultSortOrder, defaultResultsPerPage))
            .thenReturn(CompletableDeferred(result))

        // When performing a search
        val response = dataSource.search(query, page, SortOrder.RECENT, defaultResultsPerPage)

        // Then an error is reported
        assertTrue(response is Result.Error)
    }

    @Test
    fun search_whenResponseEmpty() = runBlocking {
        // Given that the service responds with success but with an empty response
        withSuccess(null)

        // When performing a search
        val response = dataSource.search(query, page, SortOrder.RECENT, defaultResultsPerPage)

        // Then an error is reported
        assertTrue(response is Result.Error)
    }

    @Test
    fun search_defaultParams() {
        // Given that the service responds with success when called
        withSuccess(shots)

        // When performing a search without specifying the sort or results per page
        runBlocking { dataSource.search(query, page) }

        // Then the default values for these params are used
        verify(service).searchDeferred(query, page, defaultSortOrder, defaultResultsPerPage)
    }

    @Test
    fun search_nonDefaultParams() {
        // Given that the service responds with success when called
        val popularSearchParam = ""
        val customPerPage = 20
        val result = Response.success(shots)
        whenever(service.searchDeferred(query, page, popularSearchParam, customPerPage))
            .thenReturn(CompletableDeferred(result))

        // When performing a search & specifying non-default sort & results per page
        runBlocking { dataSource.search(query, page, SortOrder.POPULAR, customPerPage) }

        // Then the supplied values for these params are used
        verify(service).searchDeferred(query, page, popularSearchParam, customPerPage)
    }

    private fun withSuccess(shots: List<Shot>?) {
        val result = Response.success(shots)
        whenever(service.searchDeferred(query, page, defaultSortOrder, defaultResultsPerPage))
            .thenReturn(CompletableDeferred(result))
    }
}
