package io.plaidapp.core.designernews.data.api.votes

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.api.model.User
import io.plaidapp.core.designernews.data.api.provideFakeCoroutinesContextProvider
import io.plaidapp.core.designernews.data.api.votes.model.UpvoteRequest
import io.plaidapp.core.designernews.login.data.DesignerNewsLoginRepository
import kotlinx.coroutines.experimental.CompletableDeferred
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response

/**
 * Test for [DesignerNewsVotesRepository].
 */
class DesignerNewsVotesRepositoryTest {

    val userId = 3L
    val user = User(id = userId)
    val storyId = 1345L

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val loginRepository = Mockito.mock(DesignerNewsLoginRepository::class.java)
    private val votesRepository = DesignerNewsVotesRepository(
            service,
            loginRepository,
            provideFakeCoroutinesContextProvider()
    )

    @Test
    fun upvoteStory_whenUserLoggedIn_whenRequestSuccessful() {
        // Given a logged in user
        Mockito.`when`(loginRepository.user).thenReturn(user)
        // Given that the service responds with success
        val response = Response.success(Unit)
        val request = UpvoteRequest(storyId, userId)
        Mockito.`when`(service.upvoteStoryV2(any()))
                .thenReturn(CompletableDeferred(response))
        var result: Result<Unit>? = null

        // When upvoting a story
        votesRepository.upvoteStory(storyId) { result = it }

        // Then the result is successful
        Assert.assertEquals(Result.Success(Unit), result)
        // Then the correct request was triggered
        Mockito.verify(service.upvoteStoryV2(UpvoteRequest(storyId, userId)))
    }
}

private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}
private fun <T> uninitialized(): T = null as T