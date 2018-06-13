package io.plaidapp.data.api.designernews.login

import android.text.TextUtils
import io.plaidapp.data.api.designernews.model.User

class DesignerNewsLoginRepository(private val storage: DesignerNewsLoginLocalStorage) {
    var accessToken: String? = null
        set(value) {
            if (!value.isNullOrEmpty()) {
                value?.apply {
                    isLoggedIn = true
                    storage.saveAccessToken(value)
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
            storage.saveUser(user)
            isLoggedIn = true
        }
    }

    fun logout() {
        isLoggedIn = false
        accessToken = null
        user = null
        storage.clearData()
    }
}
