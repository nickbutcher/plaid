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

package io.plaidapp.search.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.feed.FeedProgressUiModel
import io.plaidapp.search.domain.SearchDataManager
import io.plaidapp.test.shared.LiveDataTestUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations

/**
 * Tests for [SearchViewModel] that mocks the dependencies
 */
class SearchViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dataManager: SearchDataManager = mock()
    private val viewModel = SearchViewModel(dataManager)

    @Captor
    private lateinit var dataLoadingCallback: ArgumentCaptor<DataLoadingSubject.DataLoadingCallbacks>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun searchFor_searchesInDataManager() {
        // Given a query
        val query = "Plaid"

        // When searching for the query
        viewModel.searchFor(query)

        // Then search is called in data manager
        verify(dataManager).searchFor(query)
    }

    @Test
    fun loadMore_loadsInDataManager() {
        // When loading more
        viewModel.loadMore()

        // Then load more is called in data manager
        verify(dataManager).loadMore()
    }

    @Test
    fun clearResults_clearsInDataManager() {
        // When clearing results
        viewModel.clearResults()

        // Then clear results is called in data manager
        verify(dataManager).clear()
    }

    @Test
    fun dataLoading() {
        // Given a view model
        verify(dataManager).registerCallback(capture(dataLoadingCallback))

        // When data started loading
        dataLoadingCallback.value.dataStartedLoading()

        // Then the feedProgress emits true
        val progress = LiveDataTestUtil.getValue(viewModel.searchProgress)
        Assert.assertEquals(FeedProgressUiModel(true), progress)
    }

    @Test
    fun dataFinishedLoading() {
        // Given a view model
        verify(dataManager).registerCallback(capture(dataLoadingCallback))

        // When data finished loading
        dataLoadingCallback.value.dataFinishedLoading()

        // Then the feedProgress emits false
        val progress = LiveDataTestUtil.getValue(viewModel.searchProgress)
        Assert.assertEquals(FeedProgressUiModel(false), progress)
    }
}
