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

#ifndef RASTERMILL_STREAM_H
#define RASTERMILL_STREAM_H

#include <jni.h>
#include <stdio.h>
#include <sys/types.h>

class Stream {
public:
    Stream();
    virtual ~Stream();

    size_t peek(void* buffer, size_t size);
    size_t read(void* buffer, size_t size);
    virtual uint8_t* getRawBufferAddr();
    virtual jobject getRawBuffer();
    virtual int getRawBufferSize();

protected:
    virtual size_t doRead(void* buffer, size_t size) = 0;

private:
    char* mPeekBuffer;
    size_t mPeekSize;
    size_t mPeekOffset;
};

class MemoryStream : public Stream {
public:
    MemoryStream(void* buffer, size_t size, jobject buf) :
            mBuffer((uint8_t*)buffer),
            mRemaining(size),
            mRawBuffer(buf) {}
    virtual uint8_t* getRawBufferAddr();
    virtual jobject getRawBuffer();
    virtual int getRawBufferSize();

protected:
    virtual size_t doRead(void* buffer, size_t size);

private:
    uint8_t* mBuffer;
    size_t mRemaining;
    jobject mRawBuffer;
};

class FileStream : public Stream {
public:
    FileStream(FILE* fd) : mFd(fd) {}

protected:
    virtual size_t doRead(void* buffer, size_t size);

private:
    FILE* mFd;
};

class JavaInputStream : public Stream {
public:
    JavaInputStream(JNIEnv* env, jobject inputStream, jbyteArray byteArray) :
            mEnv(env),
            mInputStream(inputStream),
            mByteArray(byteArray),
            mByteArrayLength(env->GetArrayLength(byteArray)) {}

protected:
    virtual size_t doRead(void* buffer, size_t size);

private:
    JNIEnv* mEnv;
    const jobject mInputStream;
    const jbyteArray mByteArray;
    const size_t mByteArrayLength;
};

jint JavaStream_OnLoad(JNIEnv* env);

#endif //RASTERMILL_STREAM_H
