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

#ifndef RASTERMILL_FRAMESQUENCE_GIF_H
#define RASTERMILL_FRAMESQUENCE_GIF_H

#include "config.h"
#include "gif_lib.h"

#include "Stream.h"
#include "Color.h"
#include "FrameSequence.h"

class FrameSequence_gif : public FrameSequence {
public:
    FrameSequence_gif(Stream* stream);
    virtual ~FrameSequence_gif();

    virtual int getWidth() const {
        return mGif ? mGif->SWidth : 0;
    }

    virtual int getHeight() const {
        return mGif ? mGif->SHeight : 0;
    }

    virtual bool isOpaque() const {
        return (mBgColor & COLOR_8888_ALPHA_MASK) == COLOR_8888_ALPHA_MASK;
    }

    virtual int getFrameCount() const {
        return mGif ? mGif->ImageCount : 0;
    }

    virtual int getDefaultLoopCount() const {
        return mLoopCount;
    }

    virtual jobject getRawByteBuffer() const {
        return NULL;
    }

    virtual FrameSequenceState* createState() const;

    GifFileType* getGif() const { return mGif; }
    Color8888 getBackgroundColor() const { return mBgColor; }
    bool getPreservedFrame(int frameIndex) const { return mPreservedFrames[frameIndex]; }
    int getRestoringFrame(int frameIndex) const { return mRestoringFrames[frameIndex]; }

private:
    GifFileType* mGif;
    int mLoopCount;
    Color8888 mBgColor;

    // array of bool per frame - if true, frame data is used by a later DISPOSE_PREVIOUS frame
    bool* mPreservedFrames;

    // array of ints per frame - if >= 0, points to the index of the preserve that frame needs
    int* mRestoringFrames;
};

class FrameSequenceState_gif : public FrameSequenceState {
public:
    FrameSequenceState_gif(const FrameSequence_gif& frameSequence);
    virtual ~FrameSequenceState_gif();

    // returns frame's delay time in ms
    virtual long drawFrame(int frameNr,
            Color8888* outputPtr, int outputPixelStride, int previousFrameNr);

private:
    void savePreserveBuffer(Color8888* outputPtr, int outputPixelStride, int frameNr);
    void restorePreserveBuffer(Color8888* outputPtr, int outputPixelStride);

    const FrameSequence_gif& mFrameSequence;
    Color8888* mPreserveBuffer;
    int mPreserveBufferFrame;
};

#endif //RASTERMILL_FRAMESQUENCE_GIF_H
