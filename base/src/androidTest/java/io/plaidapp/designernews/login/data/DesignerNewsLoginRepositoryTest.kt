package io.plaidapp.designernews.login.data

import android.content.Context
import android.support.test.InstrumentationRegistry
import io.plaidapp.base.designernews.data.api.DesignerNewsAuthTokenHolder
import io.plaidapp.base.designernews.data.api.DesignerNewsService
import io.plaidapp.base.designernews.data.api.model.AccessToken
import io.plaidapp.base.designernews.data.api.model.User
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginLocalDataSource
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRemoteDataSource
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginRepository
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
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
    private val remoteDataSource = DesignerNewsLoginRemoteDataSource(DesignerNewsAuthTokenHolder(),
            service)

    private val repository = DesignerNewsLoginRepository(localDataSource, remoteDataSource)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun isNotLoggedIn_byDefault() {
        Assert.assertFalse(repository.isLoggedIn)
    }

    @Test
    fun isLoggedIn_afterSuccessfulLogin() {
        withLoginSuccessful()

        repository.login("user", "pass", { it -> assert(user == it) }, { Assert.fail() })

        Assert.assertTrue(repository.isLoggedIn)
    }

    @Test
    fun userEmpty_byDefault() {
        val expected = User.Builder().build()
        assert(expected == repository.user)
    }

    @Test
    fun logout() {
        repository.logout()

        Assert.assertFalse(repository.isLoggedIn)
    }

    @Test
    fun isNotLoggedIn_afterFailedLogin() {
        withLoginFailed()

        repository.login("user", "pass", { Assert.fail() }, {})

        Assert.assertFalse(repository.isLoggedIn)
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