package io.plaidapp.designernews.login.data

import android.util.Log
import io.plaidapp.BuildConfig
import io.plaidapp.designernews.data.api.DesignerNewsAuthTokenHolder
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.api.model.AccessToken
import io.plaidapp.designernews.data.api.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Remote data source for Designer News login data. Knows which API calls need to be triggered
 * for login (auth and /me) and updates the auth token after authorizing.
 */
class DesignerNewsLoginRemoteDataSource(
        val tokenHolder: DesignerNewsAuthTokenHolder,
        val service: DesignerNewsService
) {
    fun updateAuthToken(authToken: String?) {
        tokenHolder.authToken = authToken
    }

    fun logout() {
        tokenHolder.authToken = null
    }

    fun login(
            username: String,
            password: String,
            onSuccess: (user: User) -> Unit,
            onError: (error: String) -> Unit
    ) {
        val login = service.login(buildLoginParams(username, password))
        login.enqueue(object : Callback<AccessToken> {
            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                if (response.isSuccessful) {
                    tokenHolder.authToken = response.body()?.access_token
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

    private fun requestUser(onSuccess: (user: User) -> Unit, onError: (error: String) -> Unit) {
        val authedUser = service.getAuthedUser()
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
