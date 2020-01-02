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

package io.plaidapp.core.util

import android.graphics.Typeface.BOLD
import android.text.style.StyleSpan
import androidx.core.text.toSpannable
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Ported from
 * https://github.com/android/android-ktx/blob/89ee2e1cde1e1b0226ed944b9abd55cee0f9b9d4/src/androidTest/java/androidx/core/text/SpannableStringTest.kt
 */
class SpannableExtensionsTest {

    @Test
    fun plusAssign() {
        val s = "Hello, World".toSpannable()

        val bold = StyleSpan(BOLD)
        s += bold
        assertEquals(0, s.getSpanStart(bold))
        assertEquals(s.length, s.getSpanEnd(bold))
    }
}
