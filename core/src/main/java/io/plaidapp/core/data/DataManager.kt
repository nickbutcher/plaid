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

package io.plaidapp.core.data

import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem.Companion.SOURCE_DESIGNER_NEWS_POPULAR
import io.plaidapp.core.designernews.domain.LoadStoriesUseCase
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.producthunt.data.ProductHuntSourceItem.Companion.SOURCE_PRODUCT_HUNT
import io.plaidapp.core.producthunt.domain.LoadPostsUseCase
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import io.plaidapp.core.util.exhaustive
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Data class mapping the key based on which we're requesting data and the page
 */
private data class InFlightRequestData(val key: String, val page: Int)

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
class DataManager @Inject constructor(
    private val loadStories: LoadStoriesUseCase,
    private val loadPosts: LoadPostsUseCase,
    private val searchStories: SearchStoriesUseCase,
    private val shotsRepository: ShotsRepository,
    private val sourcesRepository: SourcesRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : DataLoadingSubject {

    private val parentJob = SupervisorJob()
    private val scope = CoroutineScope(dispatcherProvider.computation + parentJob)

    private val parentJobs = mutableMapOf<InFlightRequestData, Job>()

    private val loadingCount = AtomicInteger(0)
    private var loadingCallbacks = mutableListOf<DataLoadingSubject.DataLoadingCallbacks>()
    private var onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>? = null
    private lateinit var pageIndexes: MutableMap<String, Int>

    private val filterListener = object : FiltersChangedCallback() {
        override fun onFiltersChanged(changedFilter: SourceItem) {
            if (changedFilter.active) {
                loadSource(changedFilter)
            } else { // filter deactivated
                val key = changedFilter.key
                parentJobs.filter { it.key.key == key }.forEach { job ->
                    job.value.cancel()
                    parentJobs.remove(job.key)
                }
                // clear the page index for the source
                pageIndexes[key] = 0
            }
        }
    }

    init {
        sourcesRepository.registerFilterChangedCallback(filterListener)
        // build a map of source keys to pages initialized to 0
        pageIndexes = sourcesRepository.getSourcesSync().map { it.key to 0 }.toMap().toMutableMap()
    }

    fun setOnDataLoadedCallback(
        onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>?
    ) {
        this.onDataLoadedCallback = onDataLoadedCallback
    }

    private fun onDataLoaded(data: List<PlaidItem>) {
        onDataLoadedCallback?.onDataLoaded(data)
    }

    suspend fun loadMore() = withContext(dispatcherProvider.computation) {
        sourcesRepository.getSources().forEach { loadSource(it) }
    }

    fun cancelLoading() {
        parentJobs.values.forEach { it.cancel() }
        parentJobs.clear()
    }

    private fun loadSource(source: SourceItem) {
        if (source.active) {
            loadStarted()
            val page = getNextPageIndex(source.key)
            // TODO each source data loading should be delegated to a different object
            // specialized in loading that specific type of data
            val data = InFlightRequestData(source.key, page)
            when (source.key) {
                SOURCE_DESIGNER_NEWS_POPULAR -> {
                    parentJobs[data] = launchLoadDesignerNewsStories(data)
                }
                SOURCE_PRODUCT_HUNT -> {
                    parentJobs[data] = launchLoadProductHunt(data)
                }
                else -> if (source is DribbbleSourceItem) {
                    parentJobs[data] = loadDribbbleSearch(source, data)
                } else if (source is DesignerNewsSearchSourceItem) {
                    parentJobs[data] = loadDesignerNewsSearch(source, data)
                }
            }
        }
    }

    private fun getNextPageIndex(dataSource: String): Int {
        var nextPage = 1 // default to one – i.e. for newly added sources
        if (pageIndexes.containsKey(dataSource)) {
            nextPage = pageIndexes.getValue(dataSource) + 1
        }
        pageIndexes[dataSource] = nextPage
        return nextPage
    }

    private fun sourceIsEnabled(key: String): Boolean {
        return pageIndexes[key] != 0
    }

    private fun sourceLoaded(
        data: List<PlaidItem>?,
        source: String,
        request: InFlightRequestData
    ) {
        loadFinished()
        if (data != null && !data.isEmpty() && sourceIsEnabled(source)) {
            setPage(data, request.page)
            setDataSource(data, source)
            onDataLoaded(data)
        }
        parentJobs.remove(request)
    }

    private fun loadFailed(request: InFlightRequestData) {
        loadFinished()
        parentJobs.remove(request)
    }

    private fun launchLoadDesignerNewsStories(data: InFlightRequestData) = scope.launch {
        val result = loadStories(data.page)
        when (result) {
            is Result.Success -> sourceLoaded(
                result.data,
                SOURCE_DESIGNER_NEWS_POPULAR,
                data
            )
            is Result.Error -> loadFailed(data)
        }.exhaustive
    }

    private fun loadDesignerNewsSearch(
        sourceItem: DesignerNewsSearchSourceItem,
        data: InFlightRequestData
    ) = scope.launch {
        val result = searchStories(sourceItem.key, data.page)
        when (result) {
            is Result.Success -> sourceLoaded(result.data, sourceItem.key, data)
            is Result.Error -> loadFailed(data)
        }.exhaustive
    }

    private fun loadDribbbleSearch(source: DribbbleSourceItem, data: InFlightRequestData) =
        scope.launch {
            val result = shotsRepository.search(source.query, data.page)
            when (result) {
                is Result.Success -> sourceLoaded(result.data, source.key, data)
                is Result.Error -> loadFailed(data)
            }.exhaustive
        }

    private fun launchLoadProductHunt(data: InFlightRequestData) = scope.launch {
        // this API's paging is 0 based but this class (& sorting) is 1 based so adjust locally
        val result = loadPosts(data.page - 1)
        when (result) {
            is Result.Success -> sourceLoaded(result.data, SOURCE_PRODUCT_HUNT, data)
            is Result.Error -> loadFailed(data)
        }.exhaustive
    }

    override fun registerCallback(callback: DataLoadingSubject.DataLoadingCallbacks) {
        loadingCallbacks.add(callback)
    }

    private fun loadStarted() {
        if (0 == loadingCount.getAndIncrement()) {
            dispatchLoadingStartedCallbacks()
        }
    }

    private fun loadFinished() {
        if (0 == loadingCount.decrementAndGet()) {
            dispatchLoadingFinishedCallbacks()
        }
    }

    private fun setPage(items: List<PlaidItem>, page: Int) {
        items.forEach {
            // TODO make sure that at every loading this is set
//            it.page = page
        }
    }

    private fun setDataSource(items: List<PlaidItem>, dataSource: String) {
        for (item in items) {
            item.dataSource = dataSource
        }
    }

    private fun dispatchLoadingStartedCallbacks() {
        loadingCallbacks.forEach {
            it.dataStartedLoading()
        }
    }

    private fun dispatchLoadingFinishedCallbacks() {
        loadingCallbacks.forEach {
            it.dataFinishedLoading()
        }
    }
}
