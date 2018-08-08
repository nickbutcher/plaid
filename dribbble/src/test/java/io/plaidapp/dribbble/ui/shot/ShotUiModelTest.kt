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

package io.plaidapp.dribbble.ui.shot

import io.plaidapp.dribbble.testPlayer
import io.plaidapp.dribbble.testShot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShotUiModelTest {

    @Test
    fun shotWithDescription_toUiModel() {
        // Given a shot with a description
        val description = "Restitching Plaid"
        val shot = testShot.copy(description = description)

        // When this is converted to a Ui Model
        val uiModel = shot.toShotUiModelSync()

        // Then the description should be stored on the Ui Model
        assertEquals(description, uiModel.description)
        // But the formatted description should be empty
        assertEquals("", uiModel.formattedDescription)
        // And the view should not be hidden
        assertFalse(uiModel.hideDescription)
    }

    @Test
    fun shotWithoutDescription_toUiModel() {
        // Given a shot with an empty description
        val description = ""
        val shot = testShot.copy(description = description)

        // When this is converted to a Ui Model
        val uiModel = shot.toShotUiModelSync()

        // Then the description should be stored on the Ui Model
        assertEquals(description, uiModel.description)
        // And the formatted description should be empty
        assertEquals("", uiModel.formattedDescription)
        // And the view should not be hidden
        assertFalse(uiModel.hideDescription)
    }

    @Test
    fun shotWithViewCount_toUiModel() {
        // Given a shot with a view count greater than 1000
        val shot = testShot.copy(viewsCount = 123456)

        // When this is converted to a Ui Model
        val uiModel = shot.toShotUiModelSync()

        // Then the formatted view count is a simple string version
        assertEquals("123456", uiModel.formattedViewsCount)
    }

    @Test
    fun shotWithLikesCount_toUiModel() {
        // Given a shot with a like count greater than 1000
        val shot = testShot.copy(likesCount = 56789)

        // When this is converted to a Ui Model
        val uiModel = shot.toShotUiModelSync()

        // Then the formatted like count is a simple string version
        assertEquals("56789", uiModel.formattedLikesCount)
    }

    @Test
    fun shotWithUppercaseUsername_toUiModel() {
        // Given a shot with a user with an uppercase name
        val shot = testShot.copy(user = testPlayer.copy(name = "SHOUTY MCSHOUT"))

        // When this is converted to a Ui Model
        val uiModel = shot.toShotUiModelSync()

        // Then the username is lowercase
        assertEquals("shouty mcshout", uiModel.userName)
    }
}
