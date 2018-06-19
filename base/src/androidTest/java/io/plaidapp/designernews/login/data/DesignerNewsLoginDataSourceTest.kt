package io.plaidapp.designernews.login.data

import android.content.Context
import android.support.test.InstrumentationRegistry.getInstrumentation
import io.plaidapp.base.designernews.data.api.model.User
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginLocalDataSource
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [DesignerNewsLoginLocalDataSource].
 */
class DesignerNewsLoginDataSourceTest {

    private var sharedPreferences = getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private var dataSource = DesignerNewsLoginLocalDataSource(sharedPreferences)

    @After
    fun tearDown() {
        // cleanup the shared preferences after every test
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun user_default() {
        // When getting the default user from the data source
        // Then it should be null
        assertNull(dataSource.user)
    }

    @Test
    fun user_set() {
        // Given a user
        val user = User.Builder()
                .setId(3)
                .setDisplayName("name")
                .setPortraitUrl("www")
                .build()

        // When inserting it in the data source
        dataSource.user = user

        // Then it can then be retrieved
        assertTrue(areUsersEqual(user, dataSource.user))
    }

    @Test
    fun clearData() {
        // Given a user set
        val user = User.Builder()
                .setId(3)
                .setDisplayName("name")
                .setPortraitUrl("www")
                .build()
        dataSource.user = user

        // When clearing the data
        dataSource.clearData()

        // Then the user is null
        assertNull(dataSource.user)
    }

    /**
     * Until User is in kotlin, we need to manually check for equals
     */
    private fun areUsersEqual(expected: User, actual: User?): Boolean {
        return (actual == null) ||
                (expected.id == actual.id &&
                        expected.display_name == actual.display_name &&
                        expected.portrait_url == actual.portrait_url)
    }
}