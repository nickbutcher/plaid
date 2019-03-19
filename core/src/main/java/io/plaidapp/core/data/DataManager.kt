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

package io.plaidapp.core.data

import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.DesignerNewsSearchSource
import io.plaidapp.core.designernews.data.DesignerNewsSearchSource.Companion.SOURCE_DESIGNER_NEWS_POPULAR
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.domain.LoadStoriesUseCase
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.producthunt.data.ProductHuntSourceItem.Companion.SOURCE_PRODUCT_HUNT
import io.plaidapp.core.producthunt.domain.LoadPostsUseCase
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import io.plaidapp.core.util.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import retrofit2.Call
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
class DataManager(
    private val loadStoriesUseCase: LoadStoriesUseCase,
    private val loadPosts: LoadPostsUseCase,
    private val searchStoriesUseCase: SearchStoriesUseCase,
    private val shotsRepository: ShotsRepository,
    private val sourcesRepository: SourcesRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : DataLoadingSubject {

    private var parentJob = SupervisorJob()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)

    private val parentJobs = mutableMapOf<String, Job>()

    private val loadingCount = AtomicInteger(0)
    private var loadingCallbacks: MutableList<DataLoadingSubject.DataLoadingCallbacks>? = null
    private var onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>? = null
    private lateinit var pageIndexes: MutableMap<String, Int>
    private val inflightCalls = HashMap<String, Call<*>>()

    private val filterListener = object : FiltersChangedCallback() {
        override fun onFiltersChanged(changedFilter: SourceItem) {
            if (changedFilter.active) {
                loadSource(changedFilter)
            } else { // filter deactivated
                val key = changedFilter.key
                if (inflightCalls.containsKey(key)) {
                    val call = inflightCalls[key]
                    call?.cancel()
                    inflightCalls.remove(key)
                }
                loadStoriesUseCase.cancelRequestOfSource(key)
                searchStoriesUseCase.cancelRequestOfSource(key)
                // clear the page index for the source
                pageIndexes[key] = 0
            }
        }
    }

    init {
        setOnDataLoadedCallback(onDataLoadedCallback)

        sourcesRepository.registerFilterChangedCallback(filterListener)
        setupPageIndexes()
    }

    fun setOnDataLoadedCallback(
        onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>?
    ) {
        this.onDataLoadedCallback = onDataLoadedCallback
    }

    private fun onDataLoaded(data: List<PlaidItem>) {
        onDataLoadedCallback?.onDataLoaded(data)
    }

    fun loadAllDataSources() {
        sourcesRepository.getSourcesSync().forEach { loadSource(it) }
    }

    fun cancelLoading() {
        if (inflightCalls.size > 0) {
            inflightCalls.values.forEach { it.cancel() }
            inflightCalls.clear()
        }
        shotsRepository.cancelAllSearches()
        loadStoriesUseCase.cancelAllRequests()
        searchStoriesUseCase.cancelAllRequests()
        parentJob.cancelChildren()
    }

    private fun loadSource(source: SourceItem) {
        if (source.active) {
            loadStarted()
            val page = getNextPageIndex(source.key)
            when (source.key) {
                SOURCE_DESIGNER_NEWS_POPULAR -> loadDesignerNewsStories(page)
                SOURCE_PRODUCT_HUNT -> {
                    val jobId = "$page"
                    parentJobs[jobId] = launchLoadProductHunt(page, jobId)
                }
                else -> if (source is DribbbleSourceItem) {
                    loadDribbbleSearch(source, page)
                } else if (source is DesignerNewsSearchSource) {
                    loadDesignerNewsSearch(source, page)
                }
            }
        }
    }

    private fun setupPageIndexes() {
        val dateSources = sourcesRepository.getSourcesSync()
        pageIndexes = HashMap(dateSources.size)
        dateSources.forEach {
            if (pageIndexes[it.key] != null) {
                pageIndexes[it.key] = 0
            }
        }
    }

    private fun getNextPageIndex(dataSource: String): Int {
        var nextPage = 1 // default to one – i.e. for newly added sources
        if (pageIndexes.containsKey(dataSource)) {
            nextPage = pageIndexes[dataSource]?.or(0)?.plus(1) ?: 1
        }
        pageIndexes[dataSource] = nextPage
        return nextPage
    }

    private fun sourceIsEnabled(key: String): Boolean {
        return pageIndexes[key] != 0
    }

    private fun sourceLoaded(
        data: List<PlaidItem>?,
        page: Int,
        source: String
    ) {
        loadFinished()
        if (data != null && !data.isEmpty() && sourceIsEnabled(source)) {
            setPage(data, page)
            setDataSource(data, source)
            onDataLoaded(data)
        }
        inflightCalls.remove(source)
    }

    private fun loadFailed(source: String) {
        loadFinished()
        inflightCalls.remove(source)
    }

    private fun loadDesignerNewsStories(page: Int) {
        loadStoriesUseCase.invoke(page) { result, _, source ->
            if (result is Result.Success<*>) {
                sourceLoaded((result as Result.Success<List<Story>>).data, page, source)
            } else {
                loadFailed(source)
            }
            Unit
        }
    }

    private fun loadDesignerNewsSearch(source: DesignerNewsSearchSource, page: Int) {
        searchStoriesUseCase.invoke(source.key, page) { result, _, _ ->
            if (result is Result.Success<*>) {
                sourceLoaded((result as Result.Success<List<Story>>).data, page, source.key)
            } else {
                loadFailed(source.key)
            }
            Unit
        }
    }

    private fun loadDribbbleSearch(source: DribbbleSourceItem, page: Int) {
        shotsRepository.search(source.query, page) { result ->
            if (result is Result.Success<*>) {
                val (data) = result as Result.Success<List<Shot>>
                sourceLoaded(data, page, source.key)
            } else {
                loadFailed(source.key)
            }
            Unit
        }
    }

    private fun launchLoadProductHunt(page: Int, jobId: String) = scope.launch {
        // this API's paging is 0 based but this class (& sorting) is 1 based so adjust locally
        val result = loadPosts(page - 1)
        parentJobs.remove(jobId)
        when (result) {
            is Result.Success -> sourceLoaded(result.data, page, SOURCE_PRODUCT_HUNT)
            is Result.Error -> loadFailed(SOURCE_PRODUCT_HUNT)
        }.exhaustive
    }

    override fun registerCallback(callback: DataLoadingSubject.DataLoadingCallbacks) {
        if (loadingCallbacks == null) {
            loadingCallbacks = ArrayList(1)
        }
        loadingCallbacks?.add(callback)
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
        for (item in items) {
            item.page = page
        }
    }

    private fun setDataSource(items: List<PlaidItem>, dataSource: String) {
        for (item in items) {
            item.dataSource = dataSource
        }
    }

    private fun dispatchLoadingStartedCallbacks() {
        loadingCallbacks?.forEach {
            it.dataStartedLoading()
        }
    }

    private fun dispatchLoadingFinishedCallbacks() {
        loadingCallbacks?.forEach {
            it.dataFinishedLoading()
        }
    }
}
