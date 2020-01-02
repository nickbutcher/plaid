/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.search.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.Result
import io.plaidapp.core.data.SourceItem
import io.plaidapp.core.interfaces.PlaidDataSource
import io.plaidapp.core.interfaces.SearchDataSourceFactory
import io.plaidapp.search.domain.SearchDataSourceFactoriesRegistry
import io.plaidapp.search.shots
import io.plaidapp.search.testShot1
import io.plaidapp.test.shared.MainCoroutineRule
import io.plaidapp.test.shared.getOrAwaitValue
import io.plaidapp.test.shared.provideFakeCoroutinesDispatcherProvider
import io.plaidapp.test.shared.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

/**
 * Tests for [SearchViewModel] that mocks the dependencies
 */
@ExperimentalCoroutinesApi
class SearchViewModelTest {

    // Set the main coroutines dispatcher for unit testing
    @get:Rule
    var coroutinesRule = MainCoroutineRule()

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val factory = FakeSearchDataSourceFactory()
    private val registry: SearchDataSourceFactoriesRegistry = mock()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        whenever(registry.dataSourceFactories).thenReturn(setOf(factory))
    }

    @Test
    fun searchFor_searchesInDataManager() = coroutinesRule.runBlocking {
        // Given a query
        val query = "Plaid"
        // And an expected success result
        val result = Result.Success(shots)
        factory.dataSource.result = result
        val viewModel = SearchViewModel(registry,
            provideFakeCoroutinesDispatcherProvider(coroutinesRule.testDispatcher))

        // When searching for the query
        viewModel.searchFor(query)

        // Then search results emits with the data that was passed initially
        val results = viewModel.searchResults.getOrAwaitValue()
        assertEquals(results.items, result.data)
    }

    @Test
    fun loadMore_loadsInDataManager() = coroutinesRule.runBlocking {
        // Given a query
        val query = "Plaid"
        val viewModel = SearchViewModel(registry,
            provideFakeCoroutinesDispatcherProvider(coroutinesRule.testDispatcher))
        // And a search for the query
        viewModel.searchFor(query)
        // Given a result
        val moreResult = Result.Success(listOf(testShot1))
        factory.dataSource.result = moreResult

        // When loading more
        viewModel.loadMore()

        // Then search results emits with the data that was passed
        val results = viewModel.searchResults.getOrAwaitValue()
        assertEquals(results.items, moreResult.data)
    }
}

val sourceItem = object : SourceItem(
    "id", "query", 100, "name", 0, true, true
) {}

class FakeSearchDataSourceFactory : SearchDataSourceFactory {
    var dataSource = FakeDataSource()
    override fun create(query: String): PlaidDataSource {
        return dataSource
    }
}

class FakeDataSource : PlaidDataSource(sourceItem) {

    var result = Result.Success(emptyList<PlaidItem>())

    override suspend fun loadMore() {
        _items.value = result.data
    }
}
