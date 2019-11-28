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

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import java.io.File
import javax.inject.Inject

/**
 * A class responsible for resolving an image as identified by Url into a sharable [Uri].
 */
class ImageUriProvider @Inject constructor(
    context: Context,
    private val fileAuthority: FileAuthority
) {

    // Only hold the app context to avoid leaks
    private val appContext = context.applicationContext

    /**
     * Long running method! Making this a suspend function so it's only executed from a coroutine.
     *
     * Retrieve the image from Glide, as file, rename it, since Glide caches an unfriendly and
     * extension-less name, and then get the Uri.
     */
    @Suppress("RedundantSuspendModifier")
    suspend operator fun invoke(url: String, width: Int, height: Int): Uri {
        // Retrieve the image from Glide (hopefully cached) as a File
        val file = Glide.with(appContext)
            .asFile()
            .load(url)
            .submit(width, height)
            .get()
        // Glide cache uses an unfriendly & extension-less name. Massage it based on the original.
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        val renamed = File(file.parent, fileName)
        file.renameTo(renamed)
        return FileProvider.getUriForFile(appContext, fileAuthority.authority, renamed)
    }
}
