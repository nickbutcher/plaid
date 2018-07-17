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

import android.content.SharedPreferences
import androidx.core.content.edit
import io.plaidapp.core.designernews.data.users.model.User

/**
 * Local storage for Designer News login related data, implemented using SharedPreferences
 */
class LoginLocalDataSource(private val prefs: SharedPreferences) {

    /**
     * Instance of the logged in user. If missing, null is returned
     */
    var user: User?
        get() {
            val userId = prefs.getLong(KEY_USER_ID, 0L)
            val username = prefs.getString(KEY_USER_NAME, null)
            val userAvatar = prefs.getString(KEY_USER_AVATAR, null)
            if (userId == 0L && username == null && userAvatar == null) {
                return null
            }
            // TODO save the entire user
            return User(
                id = userId,
                firstName = "",
                lastName = "",
                displayName = username,
                portraitUrl = userAvatar
            )
        }
        set(value) {
            if (value != null) {
                prefs.edit {
                    putLong(KEY_USER_ID, value.id)
                    putString(KEY_USER_NAME, value.displayName)
                    putString(KEY_USER_AVATAR, value.portraitUrl)
                }
            }
        }

    /**
     * Clear all data related to this Designer News instance: user data and access token
     */
    fun logout() {
        prefs.edit {
            putLong(KEY_USER_ID, 0L)
            putString(KEY_USER_NAME, null)
            putString(KEY_USER_AVATAR, null)
        }
    }

    companion object {
        const val DESIGNER_NEWS_PREF = "DESIGNER_NEWS_PREF"
        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_USER_NAME = "KEY_USER_NAME"
        private const val KEY_USER_AVATAR = "KEY_USER_AVATAR"
    }
}
