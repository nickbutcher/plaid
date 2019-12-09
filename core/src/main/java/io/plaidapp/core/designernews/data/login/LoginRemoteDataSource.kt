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

package io.plaidapp.core.designernews.data.login

import io.plaidapp.core.BuildConfig
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import io.plaidapp.core.designernews.data.login.model.toLoggedInUser
import io.plaidapp.core.util.safeApiCall
import java.io.IOException
import javax.inject.Inject

/**
 * Remote data source for Designer News login data. Knows which API calls need to be triggered
 * for login (auth and /me) and updates the auth token after authorizing.
 */
class LoginRemoteDataSource @Inject constructor(
    private val tokenLocalDataSource: AuthTokenLocalDataSource,
    val service: DesignerNewsService
) {

    /**
     * Log out by cleaning up the auth token
     */
    fun logout() {
        tokenLocalDataSource.authToken = null
    }

    suspend fun login(username: String, password: String) = safeApiCall(
        call = { requestLogin(username, password) },
        errorMessage = "Error logging in"
    )

    private suspend fun requestLogin(username: String, password: String): Result<LoggedInUser> {
        val response = service.login(buildLoginParams(username, password))
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                val token = body.accessToken
                tokenLocalDataSource.authToken = token
                return requestUser()
            }
        }
        return Result.Error(IOException("Access token retrieval failed ${response.code()} ${response.message()}"))
    }

    private suspend fun requestUser(): Result<LoggedInUser> {
        val response = service.getAuthedUser()
        if (response.isSuccessful) {
            val users = response.body()
            if (users != null && users.isNotEmpty()) {
                return Result.Success(users[0].toLoggedInUser())
            }
        }
        return Result.Error(IOException("Failed to get authed user ${response.code()} ${response.message()}"))
    }

    private fun buildLoginParams(username: String, password: String): Map<String, String> {
        return mapOf(
            "client_id" to BuildConfig.DESIGNER_NEWS_CLIENT_ID,
            "client_secret" to BuildConfig.DESIGNER_NEWS_CLIENT_SECRET,
            "grant_type" to "password",
            "username" to username,
            "password" to password
        )
    }
}
