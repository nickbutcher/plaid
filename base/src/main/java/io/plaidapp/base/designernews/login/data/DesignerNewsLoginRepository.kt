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

package io.plaidapp.base.designernews.login.data

import android.text.TextUtils
import io.plaidapp.base.designernews.data.api.model.User
import android.util.Log
import io.plaidapp.base.BuildConfig
import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.AccessToken
import io.plaidapp.base.designernews.provideDesignerNewsService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DesignerNewsLoginRepository(private val storage: DesignerNewsLoginLocalStorage) {

    var api: DesignerNewsService

    var accessToken: String? = null
        set(value) {
            if (!value.isNullOrEmpty()) {
                value?.apply {
                    isLoggedIn = true
                    storage.accessToken = value
                }
                field = value
            }
        }

    var isLoggedIn: Boolean = false
        private set

    var user: User? = null
        private set

    init {
        accessToken = storage.accessToken
        api = provideDesignerNewsService(accessToken)

        isLoggedIn = !TextUtils.isEmpty(accessToken)
        if (isLoggedIn) {
            user = storage.user
        }
    }

    fun logout() {
        isLoggedIn = false
        accessToken = null
        user = null

        storage.clearData()
        // recreate the API, based on null token
        api = provideDesignerNewsService(accessToken)
    }


    fun login(username: String,
              password: String,
              onSuccess: (user: User) -> Unit,
              onError: (error: String) -> Unit) {

        val login = api.login(buildLoginParams(username, password))
        login.enqueue(object : Callback<AccessToken> {
            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                if (response.isSuccessful) {
                    accessToken = response.body()!!.access_token
                    // reconstruct the API based on the access token
                    api = provideDesignerNewsService(accessToken)
                    requestUser(onSuccess, onError)
                } else {
                    onError("Access token retrieval failed")
                }
            }

            override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                Log.e(javaClass.canonicalName, t.message, t)
                onError("Access token retrieval failed with ${t.message}")
            }
        })
    }

    private fun requestUser(onSuccess: (user: User) -> Unit,
                            onError: (error: String) -> Unit) {
        val authedUser = api.getAuthedUser()
        authedUser.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (!response.isSuccessful || response.body() == null ||
                        response.body()!!.isEmpty()) {
                    onError("Failed to get user")
                    return
                }
                val users = response.body()
                if (users != null && users.isNotEmpty()) {
                    setLoggedInUser(users[0])
                    onSuccess(users[0])
                } else {
                    onError("Failed to get user")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e(javaClass.canonicalName, t.message, t)
                onError("Failed to get authed user ${t.message}")
            }
        })
    }

    private fun setLoggedInUser(user: User?) {
        user?.apply {
            storage.user = user
            isLoggedIn = true
        }
    }

    private fun buildLoginParams(username: String,
                                 password: String): Map<String, String> {
        val loginParams = HashMap<String, String>(5)
        loginParams["client_id"] = BuildConfig.DESIGNER_NEWS_CLIENT_ID
        loginParams["client_secret"] = BuildConfig.DESIGNER_NEWS_CLIENT_SECRET
        loginParams["grant_type"] = "password"
        loginParams["username"] = username
        loginParams["password"] = password
        return loginParams
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsLoginRepository? = null

        fun getInstance(storage: DesignerNewsLoginLocalStorage): DesignerNewsLoginRepository =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: DesignerNewsLoginRepository(storage).also { INSTANCE = it }
                }
    }
}
