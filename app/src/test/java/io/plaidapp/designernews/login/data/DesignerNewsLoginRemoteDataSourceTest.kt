package io.plaidapp.designernews.login.data

import io.plaidapp.designernews.data.api.DesignerNewsAuthTokenHolder
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.api.model.AccessToken
import io.plaidapp.designernews.data.api.model.User
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import retrofit2.mock.Calls
import java.io.IOException


class DesignerNewsLoginRemoteDataSourceTest {

    private val user = User.Builder()
            .setId(1)
            .setDisplayName("Name")
            .setPortraitUrl("www")
            .build()

    private val accessToken = AccessToken("token", "type", "scope")

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val authHolder = DesignerNewsAuthTokenHolder()
    private val dataSource = DesignerNewsLoginRemoteDataSource(authHolder, service)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun updateAuthToken_updatesToken() {
        dataSource.updateAuthToken("token")

        assert("token" == authHolder.authToken)
    }

    @Test
    fun logout_clearsToken() {
        dataSource.logout()

        Assert.assertNull(authHolder.authToken)
    }

    @Test
    fun login_successful_when_AccessTokenAndGetUserSuccessful() {
        // Given that all API calls are successful
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(Calls.response(accessToken))
        Mockito.`when`(service.getAuthedUser()).thenReturn(Calls.response(arrayListOf(user)))

        // Login is successful
        dataSource.login(
                "test",
                "test",
                { it -> assert(user == it) },
                { Assert.fail() })
    }

    @Test
    fun login_failed_whenAccessTokenFailed() {
        // Given that the auth token retrieval fails
        val failureResponse = Calls.failure<AccessToken>(IOException("test"))
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(failureResponse)

        // Then the login fails
        dataSource.login(
                "test",
                "test",
                { Assert.fail("login network call failed so login should have failed") },
                { })

        Mockito.verify(service, Mockito.never()).getAuthedUser()
    }

    @Test
    fun login_failed_whenGetUserFailed() {
        // Given that the access token is retrieved successfully
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(Calls.response(accessToken))
        // And the get authed user failed
        val failureResponse = Calls.failure<List<User>>(IOException("test"))
        Mockito.`when`(service.getAuthedUser()).thenReturn(failureResponse)

        dataSource.login(
                "test",
                "test",
                { Assert.fail("getAuthedUser failed so login should have failed") },
                { })
    }

}