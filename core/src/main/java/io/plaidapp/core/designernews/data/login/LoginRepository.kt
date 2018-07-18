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

package io.plaidapp.core.designernews.data.login

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.users.model.User

/**
 * Repository that handles Designer News login data. It knows what data sources need to be
 * triggered to login and where to store the data, once the user was logged in.
 */
class LoginRepository(
    private val localDataSource: LoginLocalDataSource,
    private val remoteDataSource: LoginRemoteDataSource
) {

    // local cache of the user object, so we don't retrieve it from the local storage every time
    // we need it
    var user: User? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        user = localDataSource.user
    }

    fun logout() {
        user = null

        localDataSource.logout()
        remoteDataSource.logout()
    }

    suspend fun login(username: String, password: String): Result<User> {
        val result = remoteDataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    private fun setLoggedInUser(loggedInUser: User) {
        user = loggedInUser
        localDataSource.user = user
    }

    // exposing this for now, until we can remove the API usage from the DesignerNewsPrefs
    fun getService(): DesignerNewsService {
        return remoteDataSource.service
    }

    companion object {
        @Volatile
        private var INSTANCE: LoginRepository? = null

        fun getInstance(
            localDataSource: LoginLocalDataSource,
            remoteDataSource: LoginRemoteDataSource
        ): LoginRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LoginRepository(
                    localDataSource,
                    remoteDataSource
                ).also { INSTANCE = it }
            }
        }
    }
}
