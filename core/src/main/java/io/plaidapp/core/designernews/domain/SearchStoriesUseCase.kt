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

package io.plaidapp.core.designernews.domain

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.LoadSourceCallback
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.toStory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Use case that searches for stories based on a query and a page in [StoriesRepository]
 */
class SearchStoriesUseCase(
    private val storiesRepository: StoriesRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    private var parentJob = Job()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)

    private val parentJobs = mutableMapOf<String, Job>()

    operator fun invoke(query: String, page: Int, callback: LoadSourceCallback) {
        val jobId = "$query::$page"
        parentJobs[jobId] = launchRequest(query, page, callback, jobId)
    }

    private fun launchRequest(
        query: String,
        page: Int,
        callback: LoadSourceCallback,
        jobId: String
    ) = scope.launch(dispatcherProvider.computation) {
        val result = storiesRepository.search(query, page)
        parentJobs.remove(jobId)
        if (result is Result.Success) {
            val stories = result.data.map { it.toStory() }
            withContext(dispatcherProvider.main) {
                callback.sourceLoaded(stories, page, query)
            }
        } else {
            withContext(dispatcherProvider.main) { callback.loadFailed(query) }
        }
    }

    fun cancelAllRequests() {
        parentJob.cancel()
    }

    fun cancelRequestOfSource(source: String) {
        parentJobs[source].apply { this?.cancel() }
    }
}
