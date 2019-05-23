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

package io.plaidapp.designernews.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.votes.model.UpvoteStoryRequest

class UpvoteStoryWorker(appContext: Context, workerParams: WorkerParameters, private val service: DesignerNewsService)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        // Upvote the comment
        return try {

            val storyId = inputData.getLong(KEY_STORY_ID, 0)
            val userId = inputData.getLong(KEY_USER_ID, 0)

            val request = UpvoteStoryRequest(storyId, userId)
            val response = service.upvoteStoryV2(request).await()
            if (response.isSuccessful) {
                // Indicate whether the task finished successfully with the Result
                Log.v(TAG_UPVOTE, "Success")
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {

            Result.failure()
        }
    }
}
