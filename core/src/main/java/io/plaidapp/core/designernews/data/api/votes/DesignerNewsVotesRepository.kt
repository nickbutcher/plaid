package io.plaidapp.core.designernews.data.api.votes

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.api.votes.model.Upvote
import io.plaidapp.core.designernews.data.api.votes.model.UpvoteRequest
import io.plaidapp.core.designernews.data.api.votes.model.VoteLinks
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginRepository
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.IOException

class DesignerNewsVotesRepository(
        private val service: DesignerNewsService,
        private val loginRepository: DesignerNewsLoginRepository,
        private val contextProvider: CoroutinesContextProvider
) {
    fun upvoteStory(
            id: Long,
            onResult: (result: Result<Unit>) -> Unit) = launch(contextProvider.main) {
        val user = loginRepository.user
        if (user != null) {
            val request = UpvoteRequest(id, user.id)
            val response = withContext(contextProvider.io) { service.upvoteStoryV2(request) }.await()
            if (response.isSuccessful) {
                onResult(Result.Success(Unit))
                return@launch
            } else {
                onResult(Result.Error(IOException(
                        "Unable to upvote story ${response.code()} ${response.errorBody()?.string()}")))
                return@launch
            }
        }
        onResult(Result.Error(IOException("User not logged in")))
    }

    companion object {
        @Volatile
        private var INSTANCE: DesignerNewsVotesRepository? = null

        fun getInstance(
                service: DesignerNewsService,
                loginRepository: DesignerNewsLoginRepository,
                contextProvider: CoroutinesContextProvider
        ): DesignerNewsVotesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                        ?: DesignerNewsVotesRepository(service, loginRepository, contextProvider)
                                .also { INSTANCE = it }
            }
        }
    }
}