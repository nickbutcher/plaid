/*
 * Copyright (C) 2013 The Android Open Source Project
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

#include <string.h>
#include "JNIHelpers.h"
#include "utils/log.h"
#include "utils/math.h"

#include "FrameSequence_gif.h"

#define GIF_DEBUG 0

static int streamReader(GifFileType* fileType, GifByteType* out, int size) {
    Stream* stream = (Stream*) fileType->UserData;
    return (int) stream->read(out, size);
}

static Color8888 gifColorToColor8888(const GifColorType& color) {
    return ARGB_TO_COLOR8888(0xff, color.Red, color.Green, color.Blue);
}

static long getDelayMs(GraphicsControlBlock& gcb) {
    return gcb.DelayTime * 10;
}

static bool willBeCleared(const GraphicsControlBlock& gcb) {
    return gcb.DisposalMode == DISPOSE_BACKGROUND || gcb.DisposalMode == DISPOSE_PREVIOUS;
}

////////////////////////////////////////////////////////////////////////////////
// Frame sequence
////////////////////////////////////////////////////////////////////////////////

FrameSequence_gif::FrameSequence_gif(Stream* stream) :
        mLoopCount(1), mBgColor(TRANSPARENT), mPreservedFrames(NULL), mRestoringFrames(NULL) {
    mGif = DGifOpen(stream, streamReader, NULL);
    if (!mGif) {
        ALOGW("Gif load failed");
        return;
    }

    if (DGifSlurp(mGif) != GIF_OK) {
        ALOGW("Gif slurp failed");
        DGifCloseFile(mGif, NULL);
        mGif = NULL;
        return;
    }

    long durationMs = 0;
    int lastUnclearedFrame = -1;
    mPreservedFrames = new bool[mGif->ImageCount];
    mRestoringFrames = new int[mGif->ImageCount];

    GraphicsControlBlock gcb;
    for (int i = 0; i < mGif->ImageCount; i++) {
        const SavedImage& image = mGif->SavedImages[i];

        // find the loop extension pair
        for (int j = 0; (j + 1) < image.ExtensionBlockCount; j++) {
            ExtensionBlock* eb1 = image.ExtensionBlocks + j;
            ExtensionBlock* eb2 = image.ExtensionBlocks + j + 1;
            if (eb1->Function == APPLICATION_EXT_FUNC_CODE
                    // look for "NETSCAPE2.0" app extension
                    && eb1->ByteCount == 11
                    && !memcmp((const char*)(eb1->Bytes), "NETSCAPE2.0", 11)
                    // verify extension contents and get loop count
                    && eb2->Function == CONTINUE_EXT_FUNC_CODE
                    && eb2->ByteCount == 3
                    && eb2->Bytes[0] == 1) {
                mLoopCount = (int)(eb2->Bytes[2] << 8) + (int)(eb2->Bytes[1]);
            }
        }

        DGifSavedExtensionToGCB(mGif, i, &gcb);

        // timing
        durationMs += getDelayMs(gcb);

        // preserve logic
        mPreservedFrames[i] = false;
        mRestoringFrames[i] = -1;
        if (gcb.DisposalMode == DISPOSE_PREVIOUS && lastUnclearedFrame >= 0) {
            mPreservedFrames[lastUnclearedFrame] = true;
            mRestoringFrames[i] = lastUnclearedFrame;
        }
        if (!willBeCleared(gcb)) {
            lastUnclearedFrame = i;
        }
    }

#if GIF_DEBUG
    ALOGD("FrameSequence_gif created with size %d %d, frames %d dur %ld",
            mGif->SWidth, mGif->SHeight, mGif->ImageCount, durationMs);
    for (int i = 0; i < mGif->ImageCount; i++) {
        DGifSavedExtensionToGCB(mGif, i, &gcb);
        ALOGD("    Frame %d - must preserve %d, restore point %d, trans color %d",
                i, mPreservedFrames[i], mRestoringFrames[i], gcb.TransparentColor);
    }
#endif

    if (mGif->SColorMap) {
        // calculate bg color
        GraphicsControlBlock gcb;
        DGifSavedExtensionToGCB(mGif, 0, &gcb);
        if (gcb.TransparentColor == NO_TRANSPARENT_COLOR) {
            mBgColor = gifColorToColor8888(mGif->SColorMap->Colors[mGif->SBackGroundColor]);
        }
    }
}

FrameSequence_gif::~FrameSequence_gif() {
    if (mGif) {
        DGifCloseFile(mGif, NULL);
    }
    delete[] mPreservedFrames;
    delete[] mRestoringFrames;
}

FrameSequenceState* FrameSequence_gif::createState() const {
    return new FrameSequenceState_gif(*this);
}

////////////////////////////////////////////////////////////////////////////////
// draw helpers
////////////////////////////////////////////////////////////////////////////////

// return true if area of 'target' is completely covers area of 'covered'
static bool checkIfCover(const GifImageDesc& target, const GifImageDesc& covered) {
    return target.Left <= covered.Left
            && covered.Left + covered.Width <= target.Left + target.Width
            && target.Top <= covered.Top
            && covered.Top + covered.Height <= target.Top + target.Height;
}

static void copyLine(Color8888* dst, const unsigned char* src, const ColorMapObject* cmap,
                     int transparent, int width) {
    for (; width > 0; width--, src++, dst++) {
        if (*src != transparent && *src < cmap->ColorCount) {
            *dst = gifColorToColor8888(cmap->Colors[*src]);
        }
    }
}

static void setLineColor(Color8888* dst, Color8888 color, int width) {
    for (; width > 0; width--, dst++) {
        *dst = color;
    }
}

static void getCopySize(const GifImageDesc& imageDesc, int maxWidth, int maxHeight,
        GifWord& copyWidth, GifWord& copyHeight) {
    copyWidth = imageDesc.Width;
    if (imageDesc.Left + copyWidth > maxWidth) {
        copyWidth = maxWidth - imageDesc.Left;
    }
    copyHeight = imageDesc.Height;
    if (imageDesc.Top + copyHeight > maxHeight) {
        copyHeight = maxHeight - imageDesc.Top;
    }
}

////////////////////////////////////////////////////////////////////////////////
// Frame sequence state
////////////////////////////////////////////////////////////////////////////////

FrameSequenceState_gif::FrameSequenceState_gif(const FrameSequence_gif& frameSequence) :
    mFrameSequence(frameSequence), mPreserveBuffer(NULL), mPreserveBufferFrame(-1) {
}

FrameSequenceState_gif::~FrameSequenceState_gif() {
       delete[] mPreserveBuffer;
}

void FrameSequenceState_gif::savePreserveBuffer(Color8888* outputPtr, int outputPixelStride, int frameNr) {
    if (frameNr == mPreserveBufferFrame) return;

    mPreserveBufferFrame = frameNr;
    const int width = mFrameSequence.getWidth();
    const int height = mFrameSequence.getHeight();
    if (!mPreserveBuffer) {
        mPreserveBuffer = new Color8888[width * height];
    }
    for (int y = 0; y < height; y++) {
        memcpy(mPreserveBuffer + width * y,
                outputPtr + outputPixelStride * y,
                width * 4);
    }
}

void FrameSequenceState_gif::restorePreserveBuffer(Color8888* outputPtr, int outputPixelStride) {
    const int width = mFrameSequence.getWidth();
    const int height = mFrameSequence.getHeight();
    if (!mPreserveBuffer) {
        ALOGD("preserve buffer not allocated! ah!");
        return;
    }
    for (int y = 0; y < height; y++) {
        memcpy(outputPtr + outputPixelStride * y,
                mPreserveBuffer + width * y,
                width * 4);
    }
}

long FrameSequenceState_gif::drawFrame(int frameNr,
        Color8888* outputPtr, int outputPixelStride, int previousFrameNr) {

    GifFileType* gif = mFrameSequence.getGif();
    if (!gif) {
        ALOGD("Cannot drawFrame, mGif is NULL");
        return -1;
    }

#if GIF_DEBUG
    ALOGD("      drawFrame on %p nr %d on addr %p, previous frame nr %d",
            this, frameNr, outputPtr, previousFrameNr);
#endif

    const int height = mFrameSequence.getHeight();
    const int width = mFrameSequence.getWidth();

    GraphicsControlBlock gcb;

    int start = max(previousFrameNr + 1, 0);

    for (int i = max(start - 1, 0); i < frameNr; i++) {
        int neededPreservedFrame = mFrameSequence.getRestoringFrame(i);
        if (neededPreservedFrame >= 0 && (mPreserveBufferFrame != neededPreservedFrame)) {
#if GIF_DEBUG
            ALOGD("frame %d needs frame %d preserved, but %d is currently, so drawing from scratch",
                    i, neededPreservedFrame, mPreserveBufferFrame);
#endif
            start = 0;
        }
    }

    for (int i = start; i <= frameNr; i++) {
        DGifSavedExtensionToGCB(gif, i, &gcb);
        const SavedImage& frame = gif->SavedImages[i];

#if GIF_DEBUG
        bool frameOpaque = gcb.TransparentColor == NO_TRANSPARENT_COLOR;
        ALOGD("producing frame %d, drawing frame %d (opaque %d, disp %d, del %d)",
                frameNr, i, frameOpaque, gcb.DisposalMode, gcb.DelayTime);
#endif
        if (i == 0) {
            //clear bitmap
            Color8888 bgColor = mFrameSequence.getBackgroundColor();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    outputPtr[y * outputPixelStride + x] = bgColor;
                }
            }
        } else {
            GraphicsControlBlock prevGcb;
            DGifSavedExtensionToGCB(gif, i - 1, &prevGcb);
            const SavedImage& prevFrame = gif->SavedImages[i - 1];
            bool prevFrameDisposed = willBeCleared(prevGcb);

            bool newFrameOpaque = gcb.TransparentColor == NO_TRANSPARENT_COLOR;
            bool prevFrameCompletelyCovered = newFrameOpaque
                    && checkIfCover(frame.ImageDesc, prevFrame.ImageDesc);

            if (prevFrameDisposed && !prevFrameCompletelyCovered) {
                switch (prevGcb.DisposalMode) {
                case DISPOSE_BACKGROUND: {
                    Color8888* dst = outputPtr + prevFrame.ImageDesc.Left +
                            prevFrame.ImageDesc.Top * outputPixelStride;

                    GifWord copyWidth, copyHeight;
                    getCopySize(prevFrame.ImageDesc, width, height, copyWidth, copyHeight);
                    for (; copyHeight > 0; copyHeight--) {
                        setLineColor(dst, TRANSPARENT, copyWidth);
                        dst += outputPixelStride;
                    }
                } break;
                case DISPOSE_PREVIOUS: {
                    restorePreserveBuffer(outputPtr, outputPixelStride);
                } break;
                }
            }

            if (mFrameSequence.getPreservedFrame(i - 1)) {
                // currently drawn frame will be restored by a following DISPOSE_PREVIOUS draw, so
                // we preserve it
                savePreserveBuffer(outputPtr, outputPixelStride, i - 1);
            }
        }

        bool willBeCleared = gcb.DisposalMode == DISPOSE_BACKGROUND
                || gcb.DisposalMode == DISPOSE_PREVIOUS;
        if (i == frameNr || !willBeCleared) {
            const ColorMapObject* cmap = gif->SColorMap;
            if (frame.ImageDesc.ColorMap) {
                cmap = frame.ImageDesc.ColorMap;
            }

            if (cmap == NULL || cmap->ColorCount != (1 << cmap->BitsPerPixel)) {
                ALOGW("Warning: potentially corrupt color map");
            }

            const unsigned char* src = (unsigned char*)frame.RasterBits;
            Color8888* dst = outputPtr + frame.ImageDesc.Left +
                    frame.ImageDesc.Top * outputPixelStride;
            GifWord copyWidth, copyHeight;
            getCopySize(frame.ImageDesc, width, height, copyWidth, copyHeight);
            for (; copyHeight > 0; copyHeight--) {
                copyLine(dst, src, cmap, gcb.TransparentColor, copyWidth);
                src += frame.ImageDesc.Width;
                dst += outputPixelStride;
            }
        }
    }

    // return last frame's delay
    const int maxFrame = gif->ImageCount;
    const int lastFrame = (frameNr + maxFrame - 1) % maxFrame;
    DGifSavedExtensionToGCB(gif, lastFrame, &gcb);
    return getDelayMs(gcb);
}

////////////////////////////////////////////////////////////////////////////////
// Registry
////////////////////////////////////////////////////////////////////////////////

#include "Registry.h"

static bool isGif(void* header, int header_size) {
    return !memcmp(GIF_STAMP, header, GIF_STAMP_LEN)
            || !memcmp(GIF87_STAMP, header, GIF_STAMP_LEN)
            || !memcmp(GIF89_STAMP, header, GIF_STAMP_LEN);
}

static bool acceptsBuffers() {
    return false;
}

static FrameSequence* createFramesequence(Stream* stream) {
    return new FrameSequence_gif(stream);
}

static RegistryEntry gEntry = {
        GIF_STAMP_LEN,
        isGif,
        createFramesequence,
        NULL,
        acceptsBuffers,
};
static Registry gRegister(gEntry);

