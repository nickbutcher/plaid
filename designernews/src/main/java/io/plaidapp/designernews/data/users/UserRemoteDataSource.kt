/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.designernews.data.users

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.util.safeApiCall
import io.plaidapp.designernews.data.api.DesignerNewsService
import java.io.IOException
import javax.inject.Inject

/**
 * Class that requests users from the service.
 */
class UserRemoteDataSource @Inject constructor(private val service: DesignerNewsService) {

    suspend fun getUsers(userIds: List<Long>) = safeApiCall(
        call = { requestGetUsers(userIds) },
        errorMessage = "Error getting user"
    )

    private suspend fun requestGetUsers(userIds: List<Long>): Result<List<User>> {
        val requestIds = userIds.joinToString(",")

        val response = service.getUsers(requestIds)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }

        return Result.Error(
            IOException("Error getting users ${response.code()} ${response.message()}")
        )
    }
}
