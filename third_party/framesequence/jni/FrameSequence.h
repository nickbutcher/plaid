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

#ifndef RASTERMILL_FRAME_SEQUENCE_H
#define RASTERMILL_FRAME_SEQUENCE_H

#include "Stream.h"
#include "Color.h"

class FrameSequenceState {
public:
    /**
     * Produces a frame of animation in the output buffer, drawing (at minimum) the delta since
     * previousFrameNr (the current contents of the buffer), or from scratch if previousFrameNr is
     * negative
     *
     * Returns frame's delay time in milliseconds.
     */
    virtual long drawFrame(int frameNr,
            Color8888* outputPtr, int outputPixelStride, int previousFrameNr) = 0;
    virtual ~FrameSequenceState() {}
};

class FrameSequence {
public:
    /**
     * Creates a FrameSequence using data from the data stream
     *
     * Type determined by header information in the stream
     */
    static FrameSequence* create(Stream* stream);

    virtual ~FrameSequence() {}
    virtual int getWidth() const = 0;
    virtual int getHeight() const = 0;
    virtual bool isOpaque() const = 0;
    virtual int getFrameCount() const = 0;
    virtual int getDefaultLoopCount() const = 0;
    virtual jobject getRawByteBuffer() const = 0;

    virtual FrameSequenceState* createState() const = 0;
};

#endif //RASTERMILL_FRAME_SEQUENCE_H
