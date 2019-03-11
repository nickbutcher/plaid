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

package io.plaidapp.core.producthunt.domain

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.producthunt.data.api.ProductHuntRepository
import io.plaidapp.core.producthunt.data.api.model.Post
import io.plaidapp.core.producthunt.data.api.model.toPost
import io.plaidapp.core.util.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Class that knows how to load posts
 */
class LoadPostsUseCase @Inject constructor(
    private val productHuntRepository: ProductHuntRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    private var parentJob = SupervisorJob()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)

    private val parentJobs = mutableMapOf<String, Job>()

    operator fun invoke(
        page: Int,
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        val jobId = "$page"
        parentJobs[jobId] = launchRequest(page, onSuccess, onError, jobId)
    }

    /**
     * Load Product Hunt data for a specific page and return the result either in onSuccess or in
     * onError
     */
    private fun launchRequest(
        page: Int,
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit,
        jobId: String
    ) = scope.launch {
        val result = productHuntRepository.loadPosts(page)
        parentJobs.remove(jobId)
        withContext(dispatcherProvider.main) {
            when (result) {
                is Result.Success -> {
                    val posts = result.data.posts.map { it.toPost() }
                    onSuccess(posts)
                }
                is Result.Error -> onError(result.exception.toString())
            }.exhaustive
        }
    }

    fun cancelAllRequests() {
        parentJob.cancelChildren()
    }
}
