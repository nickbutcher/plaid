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

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Local storage for auth token.
 */
class AuthTokenLocalDataSource(private val prefs: SharedPreferences) {

    private var _authToken: String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    /**
     * Auth token used for requests that require authentication
     */
    var authToken: String? = _authToken
        set(value) {
            prefs.edit { putString(KEY_ACCESS_TOKEN, value) }
            field = value
        }

    fun clearData() {
        prefs.edit { putString(KEY_ACCESS_TOKEN, null) }
        authToken = null
    }

    companion object {
        const val DESIGNER_NEWS_AUTH_PREF = "DESIGNER_NEWS_AUTH_PREF"
        internal const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"

        @Volatile
        private var INSTANCE: AuthTokenLocalDataSource? = null

        fun getInstance(
            sharedPreferences: SharedPreferences
        ): AuthTokenLocalDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthTokenLocalDataSource(sharedPreferences).also {
                    INSTANCE = it
                }
            }
        }
    }
}
