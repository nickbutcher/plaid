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

package io.plaidapp.designernews.data.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.plaidapp.core.designernews.data.login.model.LoggedInUser
import io.plaidapp.test.shared.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * These tests verify [LoggedInUserDao] Room database operations for a [LoggedInUser].
 */
@RunWith(AndroidJUnit4::class)
class LoggedInUserDaoTest {
    private lateinit var database: DesignerNewsDatabase
    private lateinit var loggedInUser: LoggedInUser
    private lateinit var loggedInUserDao: LoggedInUserDao

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().context
        database = Room.inMemoryDatabaseBuilder(context, DesignerNewsDatabase::class.java).build()
        loggedInUserDao = database.loggedInUserDao()
        loggedInUser = LoggedInUser(
            id = 1L,
            displayName = "Loggy L",
            firstName = "Loggy",
            lastName = "Loggerson",
            portraitUrl = "www",
            upvotes = listOf(1L, 2L, 3L)
        )
    }

    @After fun tearDown() {
        database.close()
    }

    @Test fun insertAndGetLoggedInUser() = runBlocking {
        // Given a LoggedInUser that has been inserted into the DB
        loggedInUserDao.setLoggedInUser(loggedInUser)

        // When getting the LoggedInUser via the DAO
        val userFromDb = loggedInUserDao.getLoggedInUser().getOrAwaitValue()

        // Then the retrieved LoggedInUser matches the original LoggedInUser object
        assertEquals(loggedInUser, userFromDb)
    }

    @Test fun replaceLoggedInUser() = runBlocking {
        // Given a LoggedInUser that has been inserted into the DB
        loggedInUserDao.setLoggedInUser(loggedInUser)

        // When the user's information changes and a subsequent insert is triggered
        val updatedUser = LoggedInUser(
            id = 1L,
            displayName = "LL Cool L",
            firstName = "Loggy",
            lastName = "Loggerson",
            portraitUrl = "www",
            upvotes = listOf(1L, 2L, 3L)
        )
        loggedInUserDao.setLoggedInUser(updatedUser)

        // Then a subsequent query for the LoggedInUser should show the updated information
        val userFromDb = loggedInUserDao.getLoggedInUser().getOrAwaitValue()
        assertEquals(updatedUser, userFromDb)
    }

    @Test fun uniqueLoggedInUser() = runBlocking {
        // Given a LoggedInUser that has been inserted into the DB
        loggedInUserDao.setLoggedInUser(loggedInUser)

        // When inserting another LoggedInUser without first deleting the previous user
        val newUser = LoggedInUser(
            id = 2L,
            displayName = "Moggy M",
            firstName = "Moggy",
            lastName = "Moggerson",
            portraitUrl = "www",
            upvotes = listOf(4L, 5L, 6L)
        )
        loggedInUserDao.setLoggedInUser(newUser)

        // Then a query for LoggedInUser should return the new user
        val userFromDb = loggedInUserDao.getLoggedInUser().getOrAwaitValue()
        assertEquals(newUser, userFromDb)
    }

    @Test fun deleteLoggedInUser() = runBlocking {
        // Given a LoggedInUser that has been inserted into the DB
        loggedInUserDao.setLoggedInUser(loggedInUser)

        // When the user is deleted from the database
        loggedInUserDao.deleteLoggedInUser()

        // Then a query for the LoggedInUser should be null
        val userFromDb = loggedInUserDao.getLoggedInUser().getOrAwaitValue()
        assertNull(userFromDb)
    }
}
