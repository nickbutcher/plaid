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

package io.plaidapp.util;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

/**
 * Borrowed from github.com/romannurik/muzei
 */
public class MathUtils {

    private MathUtils() { }

    public static float constrain(float min, float max, float v) {
        return Math.max(min, Math.min(max, v));
    }

    /**
     * Given the float value of an int (such as alpha),
     *
     * @param alpha
     * @return
     */
    public static int shiftedIntFloatToByteInt(@FloatRange(from = 0f, to = 1f) float alpha) {
        return (int) (255f * alpha);
    }

    public static float shiftedByteIntToIntFloat(@IntRange(from = 0, to = 255) int alpha) {
        return (float) (alpha / 255);
    }
}
