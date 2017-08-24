/*
 *  Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.plaidapp.util.glide

import android.support.rastermill.FrameSequence
import android.support.rastermill.FrameSequenceDrawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.util.ByteBufferUtil
import java.nio.ByteBuffer

/**
 * Decodes [ByteBuffer]s into [FrameSequenceDrawableResource]s.
 */
class ByteBufferFrameSequenceGifDecoder(
        private val bitmapProvider: FrameSequenceDrawable.BitmapProvider
) : ResourceDecoder<ByteBuffer, FrameSequenceDrawable> {

    override fun handles(source: ByteBuffer, options: Options) =
            source.get(0) == G && source.get(1) == I && source.get(2) == F

    override fun decode(source: ByteBuffer, width: Int, height: Int, options: Options) =
            // Rastermill doesn't support decoding GIFs directly from ByteBuffer so convert to a ByteArray first
        FrameSequenceDrawableResource(FrameSequence.decodeByteArray(ByteBufferUtil.toBytes(source)), bitmapProvider)

    companion object {
        private const val G = 'G'.toByte()
        private const val I = 'I'.toByte()
        private const val F = 'F'.toByte()
    }
}
