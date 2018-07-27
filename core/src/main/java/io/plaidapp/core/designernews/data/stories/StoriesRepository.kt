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

package io.plaidapp.core.designernews.data.stories

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.LoadSourceCallback
import io.plaidapp.core.data.Result
import io.plaidapp.core.data.prefs.SourceManager
import io.plaidapp.core.designernews.data.stories.model.Story
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * Repository class that handles work with Designer News Stories.
 */
class StoriesRepository(
    private val remoteDataSource: StoriesRemoteDataSource,
    private val contextProvider: CoroutinesContextProvider
) {

    private val parentJobs = mutableMapOf<String, Job>()
    private val cache = mutableMapOf<Long, Story>()

    fun loadStories(page: Int, callback: LoadSourceCallback) {
        val jobId = "${SourceManager.SOURCE_DESIGNER_NEWS_POPULAR}::$page"
        parentJobs[jobId] = launchRequest(
            SourceManager.SOURCE_DESIGNER_NEWS_POPULAR,
            page,
            callback,
            jobId
        ) {
            remoteDataSource.loadStories(page)
        }
    }

    fun search(
        query: String,
        page: Int,
        callback: LoadSourceCallback
    ) {
        val jobId = "$query::$page"
        parentJobs[jobId] = launchRequest(query, page, callback, jobId) {
            remoteDataSource.search(query, page)
        }
    }

    fun getStory(id: Long): Result<Story> {
        val story = cache[id]
        return if (story != null) {
            Result.Success(story)
        } else {
            Result.Error(IllegalStateException("Story $id not cached"))
        }
    }

    private fun launchRequest(
        query: String,
        page: Int,
        callback: LoadSourceCallback,
        jobId: String,
        request: suspend () -> Result<List<Story>>
    ) = launch(contextProvider.io) {
        val result = request.invoke()
        parentJobs.remove(jobId)
        if (result is Result.Success) {
            cache(result.data)
            withContext(contextProvider.main) {
                callback.sourceLoaded(result.data, page, query)
            }
        } else {
            withContext(contextProvider.main) { callback.loadFailed(query) }
        }
    }

    fun cancelAllRequests() {
        parentJobs.values.forEach { it.cancel() }
    }

    fun cancelRequestOfSource(source: String) {
        parentJobs[source].apply { this?.cancel() }
    }

    private fun cache(data: List<Story>) {
        data.associateTo(cache) { it.id to it }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoriesRepository? = null

        fun getInstance(
            remoteDataSource: StoriesRemoteDataSource,
            contextProvider: CoroutinesContextProvider
        ): StoriesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoriesRepository(remoteDataSource, contextProvider).also {
                    INSTANCE = it
                }
            }
        }
    }
}
