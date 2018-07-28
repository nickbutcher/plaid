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

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.LoadSourceCallback
import io.plaidapp.core.data.Result
import io.plaidapp.core.data.prefs.SourceManager
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.toStory
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * Use case that loads stories from [StoriesRepository].
 */
class LoadStoriesUseCase(
    private val storiesRepository: StoriesRepository,
    private val contextProvider: CoroutinesContextProvider
) {
    private val parentJobs = mutableMapOf<String, Job>()

    operator fun invoke(page: Int, callback: LoadSourceCallback) {
        val jobId = "${SourceManager.SOURCE_DESIGNER_NEWS_POPULAR}::$page"
        parentJobs[jobId] = launchLoad(page, callback, jobId)
    }

    private fun launchLoad(
        page: Int,
        callback: LoadSourceCallback,
        jobId: String
    ) = launch(contextProvider.io) {
        val result = storiesRepository.loadStories(page)
        parentJobs.remove(jobId)
        if (result is Result.Success) {
            val stories = result.data.map { it.toStory() }
            withContext(contextProvider.main) {
                callback.sourceLoaded(stories, page, SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
            }
        } else {
            withContext(contextProvider.main) {
                callback.loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)
            }
        }
    }

    fun cancelAllRequests() {
        parentJobs.values.forEach { it.cancel() }
    }

    fun cancelRequestOfSource(source: String) {
        parentJobs[source].apply { this?.cancel() }
    }
}
