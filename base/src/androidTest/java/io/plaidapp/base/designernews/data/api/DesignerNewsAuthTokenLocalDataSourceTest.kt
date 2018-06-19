package io.plaidapp.base.designernews.data.api

import android.content.Context
import android.support.test.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for [DesignerNewsAuthTokenLocalDataSource]
 */
class DesignerNewsAuthTokenLocalDataSourceTest {
    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private var dataSource = DesignerNewsAuthTokenLocalDataSource(sharedPreferences)

    @After
    fun tearDown() {
        // cleanup the shared preferences after every test
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun authToken_default() {
        assertNull(dataSource.authToken)
    }

    @Test
    fun authToken_set() {
        // When setting an auth token
        dataSource.authToken = "my token"

        // Then the retrieved token is the correct one
        assert("my token" == dataSource.authToken)
    }

    @Test
    fun clearData() {
        // Given that an auth token was set
        dataSource.authToken = "token"

        // When clearing data
        dataSource.clearData()

        // Then the auth token is also cleared
        assertNull(dataSource.authToken)
    }
}