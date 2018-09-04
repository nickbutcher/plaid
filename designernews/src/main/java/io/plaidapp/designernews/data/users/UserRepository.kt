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

package io.plaidapp.designernews.data.users

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.users.model.User
import java.io.IOException

/**
 * Class that requests users from the remote data source and caches them, in memory.
 */
class UserRepository(private val dataSource: UserRemoteDataSource) {

    private val cachedUsers = mutableMapOf<Long, User>()

    suspend fun getUsers(ids: Set<Long>): Result<Set<User>> {
        // find the ids in the cached users first and only request the ones that we don't have yet
        val notCachedUsers = ids.filterNot { cachedUsers.containsKey(it) }
        if (notCachedUsers.isNotEmpty()) {
            getAndCacheUsers(notCachedUsers)
        }

        // compute the list of users requested
        val users = ids.mapNotNull { cachedUsers[it] }.toSet()
        if (users.isNotEmpty()) {
            return Result.Success(users)
        }
        return Result.Error(IOException("Unable to get users"))
    }

    private suspend fun getAndCacheUsers(userIds: List<Long>) {
        val result = dataSource.getUsers(userIds)

        // save the new users in the cachedUsers
        if (result is Result.Success) {
            result.data.forEach { cachedUsers[it.id] = it }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(dataSource: UserRemoteDataSource): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(dataSource).also { INSTANCE = it }
            }
        }
    }
}
