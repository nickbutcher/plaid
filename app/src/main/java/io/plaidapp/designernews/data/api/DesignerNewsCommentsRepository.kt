package io.plaidapp.designernews.data.api

import android.util.Log
import io.plaidapp.designernews.data.api.model.Comment
import kotlinx.coroutines.experimental.async

class DesignerNewsCommentsRepository(private val service: DesignerNewsService) {

    private var inProgress = false

    fun getComments(
            ids: List<String>,
            onSuccess: (comments: List<Comment>) -> Unit,
            onError: (error: String) -> Unit
    ) {
        val requestIds = ids.joinToString()
        Log.d("flo", "ids $ids req $requestIds")
        inProgress = true
        async {
            val result = service.getComments(requestIds).await()
            if (result.isSuccessful && result.body() != null) {
                onSuccess(result.body().orEmpty())
            } else {
                onError("Unable to get comments")
            }
            inProgress = false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsCommentsRepository? = null

        fun getInstance(service: DesignerNewsService): DesignerNewsCommentsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                        ?: DesignerNewsCommentsRepository(service).also { INSTANCE = it }
            }
        }
    }
}