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

package io.plaidapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.data.DataManager
import io.plaidapp.core.data.Source
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.feed.FeedProgressUiModel
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import io.plaidapp.core.ui.filter.SourceUiModel
import io.plaidapp.core.ui.filter.SourcesHighlightUiModel
import io.plaidapp.test.shared.LiveDataTestUtil
import io.plaidapp.test.shared.provideFakeCoroutinesDispatcherProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations

/**
 * Tests for [HomeViewModel], with dependencies mocked.
 */
class HomeViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val designerNewsSource = Source.DesignerNewsSearchSource(
        "query",
        true
    )
    private val designerNewsSourceUiModel = SourceUiModel(
        designerNewsSource.key,
        designerNewsSource.name,
        designerNewsSource.active,
        designerNewsSource.iconRes,
        designerNewsSource.isSwipeDismissable,
        {},
        {}
    )
    private val dribbbleSource = Source.DribbbleSearchSource("dribbble", true)
    private val dataManager: DataManager = mock()
    private val loginRepository: LoginRepository = mock()
    private val sourcesRepository: SourcesRepository = mock()

    @Captor
    private lateinit var filtersChangedCallback: ArgumentCaptor<FiltersChangedCallback>

    @Captor
    private lateinit var dataLoadingCallback: ArgumentCaptor<DataLoadingSubject.DataLoadingCallbacks>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun logoutFromDesignerNews() {
        // Given a viewmodel with empty sources
        val homeViewModel = createViewModel()
        // When logging out from designer news
        homeViewModel.logoutFromDesignerNews()

        // Then logout is called
        verify(loginRepository).logout()
    }

    @Test
    fun isDesignerNewsLoggedIn() {
        // Given a view model
        val homeViewModel = createViewModel()
        // Given a login status
        val loginStatus = false
        whenever(loginRepository.isLoggedIn).thenReturn(loginStatus)

        // When getting the login status
        val isLoggedIn = homeViewModel.isDesignerNewsUserLoggedIn()

        // The login status is the expected one
        assertEquals(loginStatus, isLoggedIn)
    }

    @Test
    fun addSources_Dribbble() = runBlocking {
        // Given a view model
        val homeViewModel = createViewModel()

        // When adding a dribbble source
        homeViewModel.addSources(dribbbleSource.query, isDribbble = true, isDesignerNews = false)

        // Then a Dribbble source is added to the repository
        val expected = listOf(dribbbleSource)
        verify(sourcesRepository).addOrMarkActiveSources(expected)
    }

    @Test
    fun addSources_DesignerNews() = runBlocking {
        // Given a view model
        val homeViewModel = createViewModel()

        // When adding a Designer News source
        homeViewModel.addSources(designerNewsSource.query, isDribbble = false, isDesignerNews = true)

        // Then a Designer News source is added to the repository
        val expected = listOf(designerNewsSource)
        verify(sourcesRepository).addOrMarkActiveSources(expected)
    }

    @Test
    fun addSources_DribbbleDesignerNews() = runBlocking {
        // Given a view model
        val homeViewModel = createViewModel()

        // When adding a dribbble and a designer news source
        homeViewModel.addSources("query", isDribbble = true, isDesignerNews = true)

        // Then two sources are added to the repository
        val expected = listOf(
            Source.DribbbleSearchSource("query", true),
            Source.DesignerNewsSearchSource("query", true)
        )
        verify(sourcesRepository).addOrMarkActiveSources(expected)
    }

    @Test
    fun filtersUpdated_newSources() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )

        // When updating the filters to a new list of sources
        filtersChangedCallback.value.onFiltersUpdated(listOf(designerNewsSource, dribbbleSource))

        // Then ui model sources are emitted
        val sources = LiveDataTestUtil.getValue(homeViewModel.sources)
        // Then all sources are highlighted
        val sourcesHighlightUiModel = SourcesHighlightUiModel(
            highlightPositions = listOf(0, 1),
            scrollToPosition = 1
        )
        assertEquals(sourcesHighlightUiModel, sources?.highlightSources!!.peek())
        // The expected sources are retrieved
        assertEquals(2, sources.sourceUiModels.size)
    }

    @Test
    fun filtersUpdated_oneNewSource() {
        // Given a view model
        val sources = mutableListOf<Source>(designerNewsSource)
        val homeViewModel = createViewModel(sources)
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )

        // When updating the filters
        sources.add(dribbbleSource)
        filtersChangedCallback.value.onFiltersUpdated(sources)

        // Then ui model sources are emitted
        val sourcesUiModel = LiveDataTestUtil.getValue(homeViewModel.sources)
        // Then all sources are highlighted
        val sourcesHighlightUiModel = SourcesHighlightUiModel(
            highlightPositions = listOf(1),
            scrollToPosition = 1
        )
        assertEquals(sourcesHighlightUiModel, sourcesUiModel?.highlightSources!!.peek())
        // The expected sources are retrieved
        assertEquals(2, sourcesUiModel.sourceUiModels.size)
    }

    @Test
    fun sourceClicked_changesSourceActiveState() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )
        // Given that filters were updated
        filtersChangedCallback.value.onFiltersUpdated(listOf(designerNewsSource))
        // Given that ui model sources are emitted
        val sources = LiveDataTestUtil.getValue(homeViewModel.sources)!!
        val uiSource = sources.sourceUiModels[0]

        // When calling sourceClicked
        uiSource.onSourceClicked(designerNewsSourceUiModel)

        // Then the source repository is called
        verify(sourcesRepository).changeSourceActiveState(designerNewsSource.key)
    }

    @Test
    fun sourceRemoved_swipeDismissable() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )
        // Given that filters were updated
        filtersChangedCallback.value.onFiltersUpdated(listOf(designerNewsSource))
        // Given that ui model sources are emitted
        val sources = LiveDataTestUtil.getValue(homeViewModel.sources)
        val uiSource = sources!!.sourceUiModels[0]

        // When calling onSourceDismissed
        uiSource.onSourceDismissed(designerNewsSourceUiModel.copy(isSwipeDismissable = true))

        // Then the source is removed
        verify(sourcesRepository).removeSource(designerNewsSource.key)
    }

    @Test
    fun sourceRemoved_notSwipeDismissable() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )
        // Given that filters were updated
        filtersChangedCallback.value.onFiltersUpdated(listOf(designerNewsSource))
        // Given that ui model sources are emitted
        val sources = LiveDataTestUtil.getValue(homeViewModel.sources)
        val uiSource = sources!!.sourceUiModels[0]

        // When calling onSourceDismissed
        uiSource.onSourceDismissed(designerNewsSourceUiModel.copy(isSwipeDismissable = false))

        // Then the source is not removed
        verify(sourcesRepository, never()).removeSource(designerNewsSource.key)
    }

    @Test
    fun filtersRemoved() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )

        // When a source was removed
        filtersChangedCallback.value.onFilterRemoved(designerNewsSource.key)

        // Then source removed value is the expected one
        val source = LiveDataTestUtil.getValue(homeViewModel.sourceRemoved)
        assertEquals(designerNewsSource.key, source)
    }

    @Test
    fun filtersChanged_activeSource() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )

        // When an active source was changed
        val activeSource = Source.DribbbleSearchSource("dribbble", true)
        filtersChangedCallback.value.onFiltersChanged(activeSource)

        // Then source removed value is null
        val source = LiveDataTestUtil.getValue(homeViewModel.sourceRemoved)
        assertNull(source)
    }

    @Test
    fun filtersChanged_inactiveSource() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(sourcesRepository).registerFilterChangedCallback(
            capture(filtersChangedCallback)
        )

        // When an inactive source was changed
        val inactiveSource = Source.DribbbleSearchSource("dribbble", false)
        filtersChangedCallback.value.onFiltersChanged(inactiveSource)

        // Then the source removed contains the inactive source
        val source = LiveDataTestUtil.getValue(homeViewModel.sourceRemoved)
        assertEquals(inactiveSource.key, source)
    }

    @Test
    fun dataLoading() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(dataManager).registerCallback(capture(dataLoadingCallback))

        // When data started loading
        dataLoadingCallback.value.dataStartedLoading()

        // Then the feedProgress emits true
        val progress = LiveDataTestUtil.getValue(homeViewModel.feedProgress)
        assertEquals(FeedProgressUiModel(true), progress)
    }

    @Test
    fun dataFinishedLoading() {
        // Given a view model
        val homeViewModel = createViewModel()
        verify(dataManager).registerCallback(capture(dataLoadingCallback))

        // When data finished loading
        dataLoadingCallback.value.dataFinishedLoading()

        // Then the feedProgress emits false
        val progress = LiveDataTestUtil.getValue(homeViewModel.feedProgress)
        assertEquals(FeedProgressUiModel(false), progress)
    }

    private fun createViewModel(
        list: List<Source> = emptyList()
    ): HomeViewModel = runBlocking {
        whenever(sourcesRepository.getSources()).thenReturn(list)
        return@runBlocking HomeViewModel(
            dataManager,
            loginRepository,
            sourcesRepository,
            provideFakeCoroutinesDispatcherProvider()
        )
    }
}
