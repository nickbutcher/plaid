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

class DesignerNewsLoginRepository(private val storage: DesignerNewsLoginLocalStorage) {

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
        isLoggedIn = !TextUtils.isEmpty(accessToken)
        if (isLoggedIn) {
            user = storage.user
        }
    }

    fun setLoggedInUser(user: User?) {
        user?.apply {
            storage.user = user
            isLoggedIn = true
        }
    }

    fun logout() {
        isLoggedIn = false
        accessToken = null
        user = null
        storage.clearData()
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
