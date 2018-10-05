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
import io.plaidapp.core.data.prefs.SourceManager
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.toStory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case that loads stories from [StoriesRepository].
 */
class LoadStoriesUseCase @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    private var parentJob = Job()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)

    private val parentJobs = mutableMapOf<String, Job>()

    operator fun invoke(page: Int, callback: LoadSourceCallback) {
        val jobId = "${SourceManager.SOURCE_DESIGNER_NEWS_POPULAR}::$page"
        parentJobs[jobId] = launchLoad(page, callback, jobId)
    }

    private fun launchLoad(
        page: Int,
        callback: LoadSourceCallback,
        jobId: String
    ) = scope.launch(dispatcherProvider.computation) {
        val result = storiesRepository.loadStories(page)
        parentJobs.remove(jobId)
        if (result is Result.Success) {
            val stories = result.data.map { it.toStory() }
            withContext(dispatcherProvider.main) {
                callback.sourceLoaded(stories, page, SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
            }
        } else {
            withContext(dispatcherProvider.main) {
                callback.loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
            }
        }
    }

    fun cancelAllRequests() {
        parentJob.cancel()
    }

    fun cancelRequestOfSource(source: String) {
        parentJobs[source].apply { this?.cancel() }
    }
}
