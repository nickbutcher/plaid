package io.plaidapp.designernews.login.data

import android.util.Log
import io.plaidapp.BuildConfig
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.api.model.AccessToken
import io.plaidapp.designernews.data.api.model.User
import io.plaidapp.designernews.provideDesignerNewsService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DesignerNewsLoginRemoteDataSource(accessToken: String? = null) {
    var accessToken = accessToken
        set(value) {
            if (!value.isNullOrEmpty()) {
                value?.apply {
                    // recreate the API, every time the access token changes
                    api = provideDesignerNewsService(field)
                }
                field = value
            }
        }

    var api: DesignerNewsService
        private set

    init {
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
}