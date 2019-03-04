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
import io.plaidapp.core.designernews.data.votes.model.UpvoteStoryRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

class UpvoteStory(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

    override val coroutineContext = Dispatchers.IO

    override suspend fun doWork(): Result = coroutineScope {
        // Upvote the comment
        try {

            val storyId = inputData.getLong(KEY_STORY_ID, 0)
            val userId = inputData.getLong(KEY_USER_ID, 0)


            val request = UpvoteStoryRequest(storyId, userId)
//        val response = service.upvoteStoryV2(request).await()
//        return if (response.isSuccessful) {
//            Result.Success(Unit)
//        } else {
//            Result.Error(
//                IOException(
//                    "Unable to upvote story ${response.code()} ${response.errorBody()?.string()}"
//                )
//            )
//        }

            // Indicate whether the task finished successfully with the Result
            Log.v(TAG_UPVOTE, "Success")
            Result.success()

        } catch (e: Exception) {

            Result.failure()
        }
    }
}
