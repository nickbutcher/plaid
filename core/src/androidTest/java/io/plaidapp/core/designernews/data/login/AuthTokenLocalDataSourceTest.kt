/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.core.designernews.data.login

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for [AuthTokenLocalDataSource] using shared preferences from instrumentation
 * context
 */
class AuthTokenLocalDataSourceTest {
    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private var dataSource = AuthTokenLocalDataSource(sharedPreferences)

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
        assertEquals("my token", dataSource.authToken)
    }

    @Test
    fun clearData() {
        // Given that an auth token was set
        dataSource.authToken = "token"

        // When clearing data
        dataSource.clearData()

        // Then the auth token is also cleared
        assertNull(dataSource.authToken)
        assertNull(sharedPreferences.getString(AuthTokenLocalDataSource.KEY_ACCESS_TOKEN, null))
    }
}
