package io.plaidapp.designernews.login.data

import android.content.Context
import android.support.test.InstrumentationRegistry
import io.plaidapp.base.designernews.data.api.DesignerNewsAuthTokenLocalDataSource
import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.AccessToken
import io.plaidapp.base.designernews.data.api.model.User
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRemoteDataSource
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import retrofit2.mock.Calls
import java.io.IOException

/**
 * Tests for DesignerNewsLoginRemoteDataSource.
 */
class DesignerNewsLoginRemoteDataSourceTest {

    private val user = User.Builder()
            .setId(1)
            .setDisplayName("Name")
            .setPortraitUrl("www")
            .build()

    private val accessToken = AccessToken("token", "type", "scope")
    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val authHolder = DesignerNewsAuthTokenLocalDataSource(sharedPreferences)
    private val dataSource = DesignerNewsLoginRemoteDataSource(authHolder, service)

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
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
        var callbackCalled = false

        // Then the login fails
        dataSource.login(
                "test",
                "test",
                { Assert.fail("login network call failed so login should have failed") },
                { callbackCalled = true })

        assert(callbackCalled)
        Mockito.verify(service, Mockito.never()).getAuthedUser()
    }

    @Test
    fun login_failed_whenGetUserFailed() {
        // Given that the access token is retrieved successfully
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(Calls.response(accessToken))
        // And the get authed user failed
        val failureResponse = Calls.failure<List<User>>(IOException("test"))
        Mockito.`when`(service.getAuthedUser()).thenReturn(failureResponse)
        var callbackCalled = false

        dataSource.login(
                "test",
                "test",
                { Assert.fail("getAuthedUser failed so login should have failed") },
                { callbackCalled = true })

        assert(callbackCalled)
    }

}