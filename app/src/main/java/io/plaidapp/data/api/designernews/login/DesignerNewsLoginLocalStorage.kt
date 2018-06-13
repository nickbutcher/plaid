package io.plaidapp.data.api.designernews.login

import android.content.SharedPreferences

import io.plaidapp.data.api.designernews.model.User

class DesignerNewsLoginLocalStorage(private val prefs: SharedPreferences) {

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    val user: User
        get() {
            val userId = prefs.getLong(KEY_USER_ID, 0L)
            val username = prefs.getString(KEY_USER_NAME, null)
            val userAvatar = prefs.getString(KEY_USER_AVATAR, null)
            return User.Builder()
                    .setId(userId)
                    .setDisplayName(username)
                    .setPortraitUrl(userAvatar)
                    .build()
        }

    fun saveAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }

    fun saveUser(user: User) {
        val editor = prefs.edit()
        editor.putLong(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.display_name)
        editor.putString(KEY_USER_AVATAR, user.portrait_url)
        editor.apply()
    }

    fun clearData() {
        val editor = prefs.edit()
        editor.putString(KEY_ACCESS_TOKEN, null)
        editor.putLong(KEY_USER_ID, 0L)
        editor.putString(KEY_USER_NAME, null)
        editor.putString(KEY_USER_AVATAR, null)
        editor.apply()
    }

    companion object {
        const val DESIGNER_NEWS_PREF = "DESIGNER_NEWS_PREF"
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_USER_NAME = "KEY_USER_NAME"
        private const val KEY_USER_AVATAR = "KEY_USER_AVATAR"
    }
}
