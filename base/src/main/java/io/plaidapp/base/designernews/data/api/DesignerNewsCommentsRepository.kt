/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.base.designernews.data.api

import io.plaidapp.base.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.async

/**
 * Repository for Designer News comments. Works with the service to get the data.
 */
class DesignerNewsCommentsRepository(private val service: DesignerNewsService) {

    private var inProgress = false

    fun getComments(
            ids: List<String>,
            onSuccess: (comments: List<Comment>) -> Unit,
            onError: (error: String) -> Unit
    ) {
        val requestIds = ids.joinToString()
        inProgress = true
        async {
            service.getComments(requestIds)
                    .await()
                    .let { result ->
                        if (result.isSuccessful && result.body() != null) {
                            onSuccess(result.body().orEmpty())
                        } else {
                            onError("Unable to get comments")
                        }
                        inProgress = false
                    }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRepository? = null

        fun getInstance(service: DesignerNewsService): DesignerNewsCommentsRepository {
            return INSTANCE
                    ?: synchronized(this) {
                INSTANCE
                        ?: DesignerNewsCommentsRepository(service).also { INSTANCE = it }
            }
        }
    }
}