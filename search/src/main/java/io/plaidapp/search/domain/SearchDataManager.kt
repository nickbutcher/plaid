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

package io.plaidapp.search.domain

import android.content.Context
import io.plaidapp.core.data.DataLoadingSubject
import io.plaidapp.core.data.LoadSourceCallback
import io.plaidapp.core.data.OnDataLoadedCallback
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.Result
import io.plaidapp.core.data.Source
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase
import io.plaidapp.core.designernews.provideSearchStoriesUseCase
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.api.model.Shot
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Responsible for loading search results from dribbble and designer news. Instantiating classes are
 * responsible for providing the [onDataLoaded] method to do something with the data.
 */
class SearchDataManager @Inject constructor(
    context: Context,
    private val onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>,
    private val shotsRepository: ShotsRepository
) : DataLoadingSubject, LoadSourceCallback {

    private val loadingCount: AtomicInteger = AtomicInteger(0)
    private val loadingCallbacks = mutableListOf<DataLoadingSubject.DataLoadingCallbacks>()

    private val searchStories: SearchStoriesUseCase = provideSearchStoriesUseCase(context)

    // state
    var query = ""
        private set
    private var page = 1

    private fun onDataLoaded(data: List<PlaidItem>) = onDataLoadedCallback.onDataLoaded(data)

    fun searchFor(newQuery: String) {
        if (query != newQuery) {
            clear()
            query = newQuery
        } else {
            page++
        }
        searchDribbble(newQuery, page)
        searchDesignerNews(newQuery, page)
    }

    fun loadMore() = searchFor(query)

    fun clear() {
        cancelLoading()
        query = ""
        page = 1
        resetLoadingCount()
    }

    fun cancelLoading() {
        searchStories.cancelAllRequests()
        shotsRepository.cancelAllSearches()
    }

    private fun searchDesignerNews(query: String, resultsPage: Int) {
        loadStarted()
        val source = Source.DesignerNewsSearchSource.DESIGNER_NEWS_QUERY_PREFIX + query
        searchStories(source, resultsPage, this)
    }

    private fun searchDribbble(query: String, resultsPage: Int) {
        loadStarted()
        shotsRepository.search(query, page) { result ->
            loadFinished()
            if (result is Result.Success<*>) {
                val shots = (result as Result.Success<List<Shot>>).data
                setPage(shots, resultsPage)
                setDataSource(
                    shots,
                    Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX + query
                )
                onDataLoaded(shots)
            }
            return@search
        }
    }

    override fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String) {
        loadFinished()
        if (result != null) {
            setPage(result, page)
            setDataSource(
                result,
                Source.DesignerNewsSearchSource.DESIGNER_NEWS_QUERY_PREFIX + query
            )
            onDataLoaded(result)
        }
    }

    override fun loadFailed(source: String) = loadFinished()

    override fun isDataLoading() = loadingCount.get() > 0

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

    private fun resetLoadingCount() {
        loadingCount.set(0)
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
