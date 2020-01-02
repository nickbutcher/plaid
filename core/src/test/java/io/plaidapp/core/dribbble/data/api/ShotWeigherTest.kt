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

package io.plaidapp.core.dribbble.data.api

import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.player
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [ShotWeigher] verifying that it applies weights to a list of [Shot]s correctly.
 */
class ShotWeigherTest {

    private val weigher = ShotWeigher()

    private val shot0 = Shot(
        likesCount = 0,
        id = 1L,
        page = 0,
        title = "Foo",
        description = "",
        images = Images(),
        user = player
    )
    private val shot20 = shot0.copy(likesCount = 20)
    private val shot99 = shot0.copy(likesCount = 99)
    private val shot200 = shot0.copy(likesCount = 200)

    @Before
    fun resetWeights() {
        listOf(shot0, shot20, shot99, shot200).forEach { it.weight = 0f }
    }

    @Test
    fun weights_inExpectedOrder() {
        // Given a number of shots with different likes counts in a random order
        val shots = listOf(shot20, shot200, shot0, shot99)

        // When the weigher weighs them
        weigher.weigh(shots)

        // Then each shot has an appropriate weight applied.
        // We don't care about the exact weight (black box test) but that they yield the
        // expected order i.e. shots with more likes have a lower weight
        assertTrue(shot200.weight < shot99.weight)
        assertTrue(shot99.weight < shot20.weight)
        assertTrue(shot20.weight < shot0.weight)
    }

    @Test
    fun weights_inExpectedRange() {
        // Given a number of shots, all with page 0
        val shots = listOf(shot20, shot200, shot0, shot99)

        // When the weigher weighs them
        weigher.weigh(shots)

        // Then weights in the range [0..1] are applied
        shots.forEach {
            assertTrue(it.weight > 0f)
            assertTrue(it.weight <= 1f)
        }
    }

    @Test
    fun weights_acrossPages_inExpectedOrder() {
        // Given a number of shots, with a range of likes and across different pages
        val shot_p0_0 = shot0.copy(page = 0)
        val shot_p0_99 = shot99.copy(page = 0)
        val shot_p1_0 = shot0.copy(page = 1)
        val shot_p1_99 = shot99.copy(page = 1)
        val shot_p2_0 = shot0.copy(page = 2)
        val shot_p2_99 = shot99.copy(page = 2)
        val shots = listOf(shot_p2_0, shot_p0_99, shot_p0_0, shot_p2_99, shot_p1_0, shot_p1_99)

        // When the weigher weighs them
        weigher.weigh(shots)

        // Then each shot has a weight applied in the expected order
        // i.e. shots with more likes have a lower weight, per page
        assertTrue(shot_p0_99.weight < shot_p0_0.weight)
        assertTrue(shot_p0_0.weight < shot_p1_99.weight)
        assertTrue(shot_p1_99.weight < shot_p1_0.weight)
        assertTrue(shot_p1_0.weight < shot_p2_99.weight)
        assertTrue(shot_p2_99.weight < shot_p2_0.weight)

        // And weights in the range [page..page + 1] are applied
        shots.forEach {
            assertTrue(it.weight > it.page)
            assertTrue(it.weight <= it.page + 1)
        }
    }
}
