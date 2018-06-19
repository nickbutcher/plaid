package io.plaidapp.designernews.login.data

import android.content.Context
import android.support.test.InstrumentationRegistry.getInstrumentation
import io.plaidapp.base.designernews.data.api.model.User
import io.plaidapp.base.designernews.login.data.DesignerNewsLoginLocalDataSource
import org.junit.After
import org.junit.Test

class DesignerNewsLoginDataSourceTest {

    private var sharedPreferences = getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private var dataSource = DesignerNewsLoginLocalDataSource(sharedPreferences)

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun user_default() {
        val expected = User.Builder().build()
        assert(expected == dataSource.user)
    }

    @Test
    fun user_set() {
        val user = User.Builder()
                .setId(3)
                .setDisplayName("name")
                .setPortraitUrl("www")
                .build()

        dataSource.user = user

        assert(user == dataSource.user)
    }

    @Test
    fun clearData() {
        val user = User.Builder()
                .setId(3)
                .setDisplayName("name")
                .setPortraitUrl("www")
                .build()
        dataSource.user = user

        dataSource.clearData()

        val expectedUser = User.Builder().build()
        assert(expectedUser == dataSource.user)
    }
}