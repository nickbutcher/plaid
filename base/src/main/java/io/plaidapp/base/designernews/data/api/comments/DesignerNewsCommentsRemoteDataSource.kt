package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

/**
 * Work with the Designer News API to get comments. The class knows how to construct the requests.
 */
class DesignerNewsCommentsRemoteDataSource(private val service: DesignerNewsService) {

    /**
     * Get a list of comments based on ids from Designer News API.
     * If the response is not successful or missing, then return a null list.
     */
    fun getComments(ids: List<String>): Deferred<List<Comment>?> = async {
        val requestIds = ids.joinToString(",")
        val response = service.getComments(requestIds).await()
        if (response.isSuccessful && response.body() != null) {
            response.body().orEmpty()
        } else {
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRemoteDataSource? = null

        fun getInstance(service: DesignerNewsService): DesignerNewsCommentsRemoteDataSource {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE
                                ?: DesignerNewsCommentsRemoteDataSource(service).also { INSTANCE = it }
                    }
        }
    }
}