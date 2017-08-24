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

#define LOG_TAG "Stream"

#include "Stream.h"

#include <string.h>

#include "JNIHelpers.h"
#include "utils/log.h"
#include "utils/math.h"

static struct {
    jmethodID read;
    jmethodID reset;
} gInputStreamClassInfo;

Stream::Stream()
    : mPeekBuffer(0)
    , mPeekSize(0)
    , mPeekOffset(0) {
}

Stream::~Stream() {
    delete mPeekBuffer;
}

size_t Stream::peek(void* buffer, size_t size) {
    size_t peek_remaining = mPeekSize - mPeekOffset;
    if (size > peek_remaining) {
        char* old_peek = mPeekBuffer;
        mPeekBuffer = new char[size];
        if (old_peek) {
            memcpy(mPeekBuffer, old_peek + mPeekOffset, peek_remaining);
            delete old_peek;
        }
        size_t read = doRead(mPeekBuffer + mPeekOffset, size - peek_remaining);
        mPeekOffset = 0;
        mPeekSize = peek_remaining + read;
    }
    size = min(size, mPeekSize - mPeekOffset);
    memcpy(buffer, mPeekBuffer + mPeekOffset, size);
    return size;
}

size_t Stream::read(void* buffer, size_t size) {
    size_t bytes_read = 0;
    size_t peek_remaining = mPeekSize - mPeekOffset;
    if (peek_remaining) {
        bytes_read = min(size, peek_remaining);
        memcpy(buffer, mPeekBuffer + mPeekOffset, bytes_read);
        mPeekOffset += bytes_read;
        if (mPeekOffset == mPeekSize) {
            delete mPeekBuffer;
            mPeekBuffer = 0;
            mPeekOffset = 0;
            mPeekSize = 0;
        }
        size -= bytes_read;
        buffer = ((char*) buffer) + bytes_read;
    }
    if (size) {
        bytes_read += doRead(buffer, size);
    }
    return bytes_read;
}

uint8_t* Stream::getRawBufferAddr() {
    return NULL;
}

jobject Stream::getRawBuffer() {
    return NULL;
}

int Stream::getRawBufferSize() {
    return 0;
}

uint8_t* MemoryStream::getRawBufferAddr() {
    return mBuffer;
}

jobject MemoryStream::getRawBuffer() {
    return mRawBuffer;
}

int MemoryStream::getRawBufferSize() {
    if (mRawBuffer != NULL) {
        return mRemaining;
    } else {
        return 0;
    }
}

size_t MemoryStream::doRead(void* buffer, size_t size) {
    size = min(size, mRemaining);
    memcpy(buffer, mBuffer, size);
    mBuffer += size;
    mRemaining -= size;
    return size;
}

size_t FileStream::doRead(void* buffer, size_t size) {
    return fread(buffer, 1, size, mFd);
}

size_t JavaInputStream::doRead(void* dstBuffer, size_t size) {
    size_t totalBytesRead = 0;

    do {
        size_t requested = min(size, mByteArrayLength);

        jint bytesRead = mEnv->CallIntMethod(mInputStream,
                gInputStreamClassInfo.read, mByteArray, 0, requested);
        if (mEnv->ExceptionCheck() || bytesRead < 0) {
            return 0;
        }

        mEnv->GetByteArrayRegion(mByteArray, 0, bytesRead, (jbyte*)dstBuffer);
        dstBuffer = (char*)dstBuffer + bytesRead;
        totalBytesRead += bytesRead;
        size -= bytesRead;
    } while (size > 0);

    return totalBytesRead;
}

jint JavaStream_OnLoad(JNIEnv* env) {
    // Skip the verbose logging on error for these, as they won't be subject
    // to obfuscators or similar and are thus unlikely to ever fail
    jclass inputStreamClazz = env->FindClass("java/io/InputStream");
    if (!inputStreamClazz) {
        return -1;
    }
    gInputStreamClassInfo.read = env->GetMethodID(inputStreamClazz, "read", "([BII)I");
    gInputStreamClassInfo.reset = env->GetMethodID(inputStreamClazz, "reset", "()V");
    if (!gInputStreamClassInfo.read || !gInputStreamClassInfo.reset) {
        return -1;
    }
    return 0;
}
