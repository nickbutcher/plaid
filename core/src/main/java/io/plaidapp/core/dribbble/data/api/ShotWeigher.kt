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

import io.plaidapp.core.data.PlaidItemSorting
import io.plaidapp.core.dribbble.data.api.model.Shot

/**
 * Utility class for applying weights to a group of [Shot]s for sorting. Weighs shots relative
 * to the most liked shot in the group.
 */
class ShotWeigher : PlaidItemSorting.PlaidItemGroupWeigher<Shot> {

    override fun weigh(shots: List<Shot>) {
        // We add 1 to the max so that weights don't 'overflow' into the next page range
        val maxLikes = (shots.maxByOrNull { it.likesCount }?.likesCount?.toFloat() ?: 0f) + 1f
        shots.forEach { shot ->
            val weight = 1f - (shot.likesCount.toFloat() / maxLikes)
            shot.weight = shot.page + weight
        }
    }
}
