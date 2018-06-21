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

package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * Repository for Designer News comments. Works with the service to get the data.
 */
class DesignerNewsCommentsRepository(private val remoteDataSource: DesignerNewsCommentsRemoteDataSource) {

    fun getComments(
            ids: List<String>,
            onSuccess: (comments: List<Comment>) -> Unit,
            onError: (error: String) -> Unit
    ) = launch(UI) {
        val layers = async { getAllComments(ids) }.await()
        if (layers != null && layers.isNotEmpty()) {
            for (index: Int in layers.size - 1 downTo 1) {
                update(layers[index - 1], layers[index])
            }
            onSuccess(layers[0])
        } else {
            onError("Unable to get comments")
        }
    }

    private suspend fun getAllComments(
            ids: List<String>
    ): List<List<Comment>>? = withContext(CommonPool) {
        val commentsLayers = mutableListOf<List<Comment>>()
        var newLayer = remoteDataSource.getComments(ids).await()
        while (newLayer != null) {
            commentsLayers.add(newLayer)
            val newIds = getCommentIds(newLayer)
            newLayer = if (!newIds.isEmpty()) {
                remoteDataSource.getComments(newIds).await()
            } else {
                null
            }
        }
        commentsLayers
    }

    private fun update(parents: List<Comment>, children: List<Comment>): List<Comment> {
        for (child in children) {
            for (parent in parents) {
                if (parent.id == child.links.parentComment) {
                    parent.addComment(child)
                    break
                }
            }
        }
        return parents
    }

    private fun getCommentIds(comments: List<Comment>): List<String> {
        val ids = mutableListOf<String>()
        for (comment in comments) {
            if (comment.links != null) {
                ids.addAll(comment.links.comments)
            }
        }
        return ids
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRepository? = null

        fun getInstance(
                remoteDataSource: DesignerNewsCommentsRemoteDataSource
        ): DesignerNewsCommentsRepository {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE
                                ?: DesignerNewsCommentsRepository(remoteDataSource).also { INSTANCE = it }
                    }
        }
    }
}