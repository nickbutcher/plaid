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

package io.plaidapp.dribbble.domain

import android.net.Uri
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.util.ImageUriProvider
import javax.inject.Inject

/**
 * A UseCase which prepares the information required to share a shot.
 */
class GetShareShotInfoUseCase @Inject constructor(private val imageUriProvider: ImageUriProvider) {

    suspend operator fun invoke(shot: Shot): ShareShotInfo {
        val url = shot.images.best()
        val imageSize = shot.images.bestSize()
        val uri = imageUriProvider(url, imageSize.width, imageSize.height)
        val text = "“${shot.title}” by ${shot.user.name}\n${shot.url}"
        val mime = getImageMimeType(url)
        return ShareShotInfo(uri, shot.title, text, mime)
    }

    private fun getImageMimeType(fileName: String): String {
        if (fileName.endsWith(".png")) {
            return "image/png"
        } else if (fileName.endsWith(".gif")) {
            return "image/gif"
        }
        return "image/jpeg"
    }
}

/**
 * Models information about a shot needed to share it.
 */
data class ShareShotInfo(
    val imageUri: Uri,
    val title: String,
    val shareText: CharSequence,
    val mimeType: String
)
