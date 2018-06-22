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

package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Work with the Designer News API to get comments. The class knows how to construct the requests.
 */
class DesignerNewsCommentsRemoteDataSource(
    private val service: DesignerNewsService,
    private val ioContext: CoroutineContext = CommonPool
) {

    /**
     * Get a list of comments based on ids from Designer News API.
     * If the response is not successful or missing, then return a null list.
     */
    fun getComments(ids: List<Long>): Deferred<List<Comment>?> {
        return async(ioContext) {
            val requestIds = ids.joinToString(",")
            val response = service.getComments(requestIds).await()
            if (response.isSuccessful && response.body() != null) {
                response.body().orEmpty()
            } else {
                null
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRemoteDataSource? = null

        fun getInstance(service: DesignerNewsService): DesignerNewsCommentsRemoteDataSource {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE ?: DesignerNewsCommentsRemoteDataSource(
                                service
                        ).also { INSTANCE = it }
                    }
        }
    }
}
