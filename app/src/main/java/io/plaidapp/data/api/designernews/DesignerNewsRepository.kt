/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.plaidapp.data.api.designernews

import io.plaidapp.BuildConfig
import io.plaidapp.data.LoadSourceCallback
import io.plaidapp.data.api.designernews.model.Story
import io.plaidapp.data.prefs.DesignerNewsPrefs
import io.plaidapp.data.prefs.SourceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class that handles work with Designer News.
 */
class DesignerNewsRepository(
        val service: DesignerNewsService,
        val preferences: DesignerNewsPrefs) {
    private val inflight: MutableMap<String, Call<*>> = HashMap()

    fun loadTopStories(page: Int, callback: LoadSourceCallback) {
        val topStories = if (BuildConfig.DESIGNER_NEWS_V2)
            service.getTopStoriesV2(page) else
            service.getTopStories(page)
        topStories.enqueue(object : Callback<List<Story>> {
            override fun onResponse(call: Call<List<Story>>, response: Response<List<Story>>) {
                if (response.isSuccessful) {
                    callback.sourceLoaded(response.body(), page, SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
                } else {
                    inflight.remove(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
                    callback.loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
                }
            }

            override fun onFailure(call: Call<List<Story>>, t: Throwable) {
                inflight.remove(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
                callback.loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
            }
        })
        inflight[SourceManager.SOURCE_DESIGNER_NEWS_POPULAR] = topStories
    }

    fun loadRecent(page: Int, callback: LoadSourceCallback) {
        val recentStoriesCall = if (BuildConfig.DESIGNER_NEWS_V2)
            service.getRecentStoriesV2(page) else
            service.getRecentStories(page)
        recentStoriesCall.enqueue(object : Callback<List<Story>> {
            override fun onResponse(call: Call<List<Story>>, response: Response<List<Story>>) {
                if (response.isSuccessful) {
                    callback.sourceLoaded(response.body(), page, SourceManager.SOURCE_DESIGNER_NEWS_RECENT)
                } else {
                    callback.loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_RECENT)
                }
            }

            override fun onFailure(call: Call<List<Story>>, t: Throwable) {
                callback.loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_RECENT)
            }
        })
        inflight[SourceManager.SOURCE_DESIGNER_NEWS_RECENT] = recentStoriesCall
    }

    fun search(query: String,
               page: Int,
               callback: LoadSourceCallback) {
        val searchCall = service.search(query, page)
        searchCall.enqueue(object : Callback<List<Story>> {
            override fun onResponse(call: Call<List<Story>>, response: Response<List<Story>>) {
                if (response.isSuccessful) {
                    callback.sourceLoaded(response.body(), page, query)
                } else {
                    callback.loadFailed(query)
                }
            }

            override fun onFailure(call: Call<List<Story>>, t: Throwable) {
                callback.loadFailed(query)
            }
        })

        inflight[query] = searchCall
    }

    fun cancelAllRequests() {
        for (request in inflight.values) request.cancel()
    }

    fun cancelRequestOfSource(source: String) {
        inflight[source].apply { this?.cancel() }
    }
}
