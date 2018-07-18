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

package `in`.uncod.android.bypass

import `in`.uncod.android.bypass.style.ImageLoadingSpan

/**
 * Called when an image url was found in parsed markdown.
 * This image has to be loaded in order to display.
 */
interface LoadImageCallback {
    /**
     * Callback called when an image found in a markdown document should be loaded.
     * @param src The source (url) of the image.
     * @param loadingSpan A placeholder span making where the image should be inserted.
     */
    fun loadImage(src: String, loadingSpan: ImageLoadingSpan)
}
