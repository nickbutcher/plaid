/*
 * Copyright 2018 Google, Inc.
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
import androidx.test.InstrumentationRegistry.getInstrumentation
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for [LoginLocalDataSource] using shared preferences from instrumentation
 * context
 */
class LoginLocalDataSourceTest {

    private var sharedPreferences = getInstrumentation().context
        .getSharedPreferences("test", Context.MODE_PRIVATE)

    private var dataSource = LoginLocalDataSource(sharedPreferences)

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
        val user = LoggedInUser(
            id = 3,
            firstName = "Pladinium",
            lastName = "Plaidescu",
            displayName = "Plaidinium Plaidescu",
            portraitUrl = "www",
            upvotes = listOf(1L, 2L)
        )

        // When inserting it in the data source
        dataSource.user = user

        // Then it can then be retrieved
        val expected = LoggedInUser(
            id = 3,
            firstName = "",
            lastName = "",
            displayName = "Plaidinium Plaidescu",
            portraitUrl = "www",
            upvotes = emptyList()
        )
        assertEquals(expected, dataSource.user)
    }

    @Test
    fun logout() {
        // Given a user set
        val user = LoggedInUser(
            id = 3,
            firstName = "Plaidy",
            lastName = "Plaidinkski",
            displayName = "Plaidy Plaidinski",
            portraitUrl = "www",
            upvotes = listOf(123L, 234L, 345L)
        )
        dataSource.user = user

        // When logging out
        dataSource.logout()

        // Then the user is null
        assertNull(dataSource.user)
    }
}
