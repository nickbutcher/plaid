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

@file:Suppress("NOTHING_TO_INLINE")

package io.plaidapp.core.util

import android.text.Spannable
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE

/**
 * Adds [span] to the entire text.
 *
 * Ported from
 * https://github.com/android/android-ktx/blob/89ee2e1cde1e1b0226ed944b9abd55cee0f9b9d4/src/main/java/androidx/core/text/SpannableString.kt#L32
 */
inline operator fun Spannable.plusAssign(span: Any) = setSpan(span, 0, length, SPAN_INCLUSIVE_EXCLUSIVE)