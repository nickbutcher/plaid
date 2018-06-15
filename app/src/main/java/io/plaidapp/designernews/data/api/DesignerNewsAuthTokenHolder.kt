package io.plaidapp.designernews.data.api

/**
 * Holder for Designer News auth token. Since some APIs can be triggered without requiring
 * authentication, the token can be missing.
 */
class DesignerNewsAuthTokenHolder(var authToken: String? = null) {
    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsAuthTokenHolder? = null

        fun getInstance(authToken: String?): DesignerNewsAuthTokenHolder {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                        ?: DesignerNewsAuthTokenHolder(authToken).also { INSTANCE = it }
            }
        }
    }
}