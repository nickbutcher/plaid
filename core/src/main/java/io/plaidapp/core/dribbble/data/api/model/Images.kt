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

package io.plaidapp.core.dribbble.data.api.model

import android.os.Parcelable
import android.support.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Models links to the various quality of images of a shot.
 */
@Keep
@Parcelize
data class Images(
    val hidpi: String? = null,
    val normal: String? = null,
    val teaser: String? = null
) : Parcelable {

    fun best(): String? {
        return if (!hidpi.isNullOrEmpty()) hidpi else normal
    }

    fun bestSize(): IntArray {
        return if (!hidpi.isNullOrEmpty()) TWO_X_IMAGE_SIZE else NORMAL_IMAGE_SIZE
    }

    companion object {

        private val NORMAL_IMAGE_SIZE = intArrayOf(400, 300)
        private val TWO_X_IMAGE_SIZE = intArrayOf(800, 600)
    }
}
