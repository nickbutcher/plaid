/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.data.api.dribbble;

import java.util.List;

import io.plaidapp.data.PlaidItemSorting;
import io.plaidapp.data.api.dribbble.model.Shot;

/**
 * Utility class for applying weights to a group of {@link Shot}s for sorting. Weighs shots relative
 * to the most liked shot in the group.
 */
public class ShotWeigher implements PlaidItemSorting.PlaidItemGroupWeigher<Shot> {

    @Override
    public void weigh(List<Shot> shots) {
        float maxLikes = 0f;
        for (Shot shot : shots) {
            maxLikes = Math.max(maxLikes, shot.likes_count);
        }
        for (Shot shot : shots) {
            float weight = 1f - ((float) shot.likes_count / maxLikes);
            shot.weight = shot.page + weight;
        }
    }

}
