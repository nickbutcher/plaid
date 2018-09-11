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

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import io.plaidapp.core.designernews.data.database.DesignerNewsDatabase
import io.plaidapp.core.designernews.data.database.LoggedInUserDao
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for [LoginLocalDataSource] using the on device database.
 */
class LoginLocalDataSourceTest {

    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().context,
        DesignerNewsDatabase::class.java
    ).allowMainThreadQueries()
        .build()

    private val dao: LoggedInUserDao = database.loggedInUserDao()
    private var dataSource = LoginLocalDataSource(dao)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun user_default() = runBlocking {
        // When getting the default user from the data source
        // Then it should be null
        assertNull(dataSource.getUser())
    }

    @Test
    fun user_set() = runBlocking {
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
        dataSource.setUser(user)

        // Then it can then be retrieved
        assertEquals(user, dataSource.getUser())
    }

    @Test
    fun logout() = runBlocking {
        // Given a user set
        val user = LoggedInUser(
            id = 3,
            firstName = "Plaidy",
            lastName = "Plaidinkski",
            displayName = "Plaidy Plaidinski",
            portraitUrl = "www",
            upvotes = listOf(123L, 234L, 345L)
        )
        dataSource.setUser(user)

        // When logging out
        dataSource.logout()

        // Then the user is null
        assertNull(dataSource.getUser())
    }
}
