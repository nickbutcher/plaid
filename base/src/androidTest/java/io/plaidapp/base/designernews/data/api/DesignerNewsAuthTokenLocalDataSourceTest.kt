package io.plaidapp.base.designernews.data.api

import android.content.Context
import android.support.test.InstrumentationRegistry
import org.junit.After
import org.junit.Assert
import org.junit.Test

class DesignerNewsAuthTokenLocalDataSourceTest {
    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private var dataSource = DesignerNewsAuthTokenLocalDataSource(sharedPreferences)

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun authToken_default() {
        Assert.assertNull(dataSource.authToken)
    }

    @Test
    fun authToken_set() {
        dataSource.authToken = "my token"

        assert("my token" == dataSource.authToken)
    }

    @Test
    fun clearData() {
        dataSource.authToken = "token"

        dataSource.clearData()

        Assert.assertNull(dataSource.authToken)
    }
}