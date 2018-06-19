package io.plaidapp.designernews.login.data

import android.content.Context
import android.support.test.InstrumentationRegistry
import io.plaidapp.base.designernews.data.api.DesignerNewsAuthTokenLocalDataSource
import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.AccessToken
import io.plaidapp.base.designernews.data.api.model.User
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginLocalDataSource
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRemoteDataSource
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRepository
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import retrofit2.mock.Calls
import java.io.IOException

class DesignerNewsLoginRepositoryTest {

    private val user = User.Builder()
            .setId(1)
            .setDisplayName("Name")
            .setPortraitUrl("www")
            .build()

    private val accessToken = AccessToken("token", "type", "scope")

    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)
    private var localDataSource = DesignerNewsLoginLocalDataSource(sharedPreferences)

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val remoteDataSource = DesignerNewsLoginRemoteDataSource(
            DesignerNewsAuthTokenLocalDataSource(sharedPreferences), service)

    private val repository = DesignerNewsLoginRepository(localDataSource, remoteDataSource)

    @After
    fun tearDown() {
        // cleanup the shared preferences after every test
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun isNotLoggedIn_byDefault() {
        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun isLoggedIn_afterSuccessfulLogin() {
        // Given that the login will be successful
        withLoginSuccessful()
        var callbackCalled = false

        // When logging in
        repository.login(
                "user",
                "pass",
                { it ->
                    callbackCalled = true
                    assert(user == it)
                },
                { Assert.fail() })

        // Then the success callback was called
        assertTrue(callbackCalled)
        // The user is logged in
        assertTrue(repository.isLoggedIn)
    }

    @Test
    fun userNull_byDefault() {
        assertNull(repository.user)
    }

    @Test
    fun logout() {
        // When logging out
        repository.logout()

        // Then the user is not logged in
        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun logout_afterLogin() {
        // Given a logged in user
        withLoginSuccessful()
        repository.login("user", "pass", { it -> assert(user == it) }, { Assert.fail() })

        // When logging out
        repository.logout()

        // Then the user is logged out
        assertFalse(repository.isLoggedIn)
    }

    @Test
    fun isNotLoggedIn_afterFailedLogin() {
        // Given that the login will fail
        withLoginFailed()
        var callbackCalled = false

        // When logging in
        repository.login("user", "pass", { Assert.fail() }, { callbackCalled = true })

        // Then the error callback was called
        assertTrue(callbackCalled)
        // The user is not logged in
        assertFalse(repository.isLoggedIn)
    }

    private fun withLoginSuccessful() {
        Mockito.`when`(service.login(Mockito.anyMap())).thenReturn(Calls.response(accessToken))
        Mockito.`when`(service.getAuthedUser()).thenReturn(Calls.response(arrayListOf(user)))
    }

    private fun withLoginFailed() {
        Mockito.`when`(service.login(Mockito.anyMap()))
                .thenReturn(Calls.failure(IOException("test")))
    }
}