package io.plaidapp.base.designernews.data.api

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Local storage for auth token.
 */
class DesignerNewsAuthTokenLocalDataSource(private val prefs: SharedPreferences) {

    private var _authToken: String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    /**
     * Auth token used for requests that require authentication
     */
    var authToken: String? = _authToken
        set(value) {
            prefs.edit { putString(KEY_ACCESS_TOKEN, value) }
            _authToken = value
        }

    fun clearData() {
        prefs.edit { KEY_ACCESS_TOKEN to null }
    }

    companion object {
        const val DESIGNER_NEWS_AUTH_PREF = "DESIGNER_NEWS_AUTH_PREF"
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"

        @Volatile
        private var INSTANCE: DesignerNewsAuthTokenLocalDataSource? = null

        fun getInstance(sharedPreferences: SharedPreferences): DesignerNewsAuthTokenLocalDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                        ?: DesignerNewsAuthTokenLocalDataSource(sharedPreferences).also { INSTANCE = it }
            }
        }
    }

}