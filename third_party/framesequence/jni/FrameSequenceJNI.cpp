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

#include <android/bitmap.h>
#include "JNIHelpers.h"
#include "utils/log.h"
#include "FrameSequence.h"

#include "FrameSequenceJNI.h"

#define JNI_PACKAGE "android/support/rastermill"

static struct {
    jclass clazz;
    jmethodID ctor;
} gFrameSequenceClassInfo;

////////////////////////////////////////////////////////////////////////////////
// Frame sequence
////////////////////////////////////////////////////////////////////////////////

static jobject createJavaFrameSequence(JNIEnv* env, FrameSequence* frameSequence) {
    if (!frameSequence) {
        return NULL;
    }
    return env->NewObject(gFrameSequenceClassInfo.clazz, gFrameSequenceClassInfo.ctor,
            reinterpret_cast<jlong>(frameSequence),
            frameSequence->getWidth(),
            frameSequence->getHeight(),
            frameSequence->isOpaque(),
            frameSequence->getFrameCount(),
            frameSequence->getDefaultLoopCount());
}

static jobject nativeDecodeByteArray(JNIEnv* env, jobject clazz,
        jbyteArray byteArray, jint offset, jint length) {
    jbyte* bytes = reinterpret_cast<jbyte*>(env->GetPrimitiveArrayCritical(byteArray, NULL));
    if (bytes == NULL) {
        jniThrowException(env, ILLEGAL_STATE_EXEPTION,
                "couldn't read array bytes");
        return NULL;
    }
    MemoryStream stream(bytes + offset, length, NULL);
    FrameSequence* frameSequence = FrameSequence::create(&stream);
    env->ReleasePrimitiveArrayCritical(byteArray, bytes, 0);
    return createJavaFrameSequence(env, frameSequence);
}

static jobject nativeDecodeByteBuffer(JNIEnv* env, jobject clazz,
        jobject buf, jint offset, jint limit) {
    jobject globalBuf = env->NewGlobalRef(buf);
    JavaVM* vm;
    env->GetJavaVM(&vm);
    MemoryStream stream(
        (reinterpret_cast<uint8_t*>(
            env->GetDirectBufferAddress(globalBuf))) + offset,
        limit,
        globalBuf);
    FrameSequence* frameSequence = FrameSequence::create(&stream);
    jobject finalSequence = createJavaFrameSequence(env, frameSequence);
    return finalSequence;
}

static jobject nativeDecodeStream(JNIEnv* env, jobject clazz,
        jobject istream, jbyteArray byteArray) {
    JavaInputStream stream(env, istream, byteArray);
    FrameSequence* frameSequence = FrameSequence::create(&stream);
    return createJavaFrameSequence(env, frameSequence);
}

static void nativeDestroyFrameSequence(JNIEnv* env, jobject clazz,
        jlong frameSequenceLong) {
    FrameSequence* frameSequence = reinterpret_cast<FrameSequence*>(frameSequenceLong);
    jobject buf = frameSequence->getRawByteBuffer();
    if (buf != NULL) {
        env->DeleteGlobalRef(buf);
    }
    delete frameSequence;
}

static jlong nativeCreateState(JNIEnv* env, jobject clazz, jlong frameSequenceLong) {
    FrameSequence* frameSequence = reinterpret_cast<FrameSequence*>(frameSequenceLong);
    FrameSequenceState* state = frameSequence->createState();
    return reinterpret_cast<jlong>(state);
}

////////////////////////////////////////////////////////////////////////////////
// Frame sequence state
////////////////////////////////////////////////////////////////////////////////

static void nativeDestroyState(
        JNIEnv* env, jobject clazz, jlong frameSequenceStateLong) {
    FrameSequenceState* frameSequenceState =
            reinterpret_cast<FrameSequenceState*>(frameSequenceStateLong);
    delete frameSequenceState;
}

void throwIae(JNIEnv* env, const char* message, int errorCode) {
    char buf[256];
    snprintf(buf, sizeof(buf), "%s, error %d", message, errorCode);
    jniThrowException(env, ILLEGAL_STATE_EXEPTION, buf);
}

static jlong JNICALL nativeGetFrame(
        JNIEnv* env, jobject clazz, jlong frameSequenceStateLong, jint frameNr,
        jobject bitmap, jint previousFrameNr) {
    FrameSequenceState* frameSequenceState =
            reinterpret_cast<FrameSequenceState*>(frameSequenceStateLong);
    int ret;
    AndroidBitmapInfo info;
    void* pixels;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        throwIae(env, "Couldn't get info from Bitmap", ret);
        return 0;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        throwIae(env, "Bitmap pixels couldn't be locked", ret);
        return 0;
    }

    int pixelStride = info.stride >> 2;
    jlong delayMs = frameSequenceState->drawFrame(frameNr,
            (Color8888*) pixels, pixelStride, previousFrameNr);

    AndroidBitmap_unlockPixels(env, bitmap);
    return delayMs;
}

static JNINativeMethod gMethods[] = {
    {   "nativeDecodeByteArray",
        "([BII)L" JNI_PACKAGE "/FrameSequence;",
        (void*) nativeDecodeByteArray
    },
    {   "nativeDecodeByteBuffer",
        "(Ljava/nio/ByteBuffer;II)L" JNI_PACKAGE "/FrameSequence;",
        (void*) nativeDecodeByteBuffer
    },
    {   "nativeDecodeStream",
        "(Ljava/io/InputStream;[B)L" JNI_PACKAGE "/FrameSequence;",
        (void*) nativeDecodeStream
    },
    {   "nativeDestroyFrameSequence",
        "(J)V",
        (void*) nativeDestroyFrameSequence
    },
    {   "nativeCreateState",
        "(J)J",
        (void*) nativeCreateState
    },
    {   "nativeGetFrame",
        "(JILandroid/graphics/Bitmap;I)J",
        (void*) nativeGetFrame
    },
    {   "nativeDestroyState",
        "(J)V",
        (void*) nativeDestroyState
    },
};

jint FrameSequence_OnLoad(JNIEnv* env) {
    // Get jclass with env->FindClass.
    // Register methods with env->RegisterNatives.
    gFrameSequenceClassInfo.clazz = env->FindClass(JNI_PACKAGE "/FrameSequence");
    if (!gFrameSequenceClassInfo.clazz) {
        ALOGW("Failed to find " JNI_PACKAGE "/FrameSequence");
        return -1;
    }
    gFrameSequenceClassInfo.clazz = (jclass)env->NewGlobalRef(gFrameSequenceClassInfo.clazz);

    gFrameSequenceClassInfo.ctor = env->GetMethodID(gFrameSequenceClassInfo.clazz, "<init>", "(JIIZII)V");
    if (!gFrameSequenceClassInfo.ctor) {
        ALOGW("Failed to find constructor for FrameSequence - was it stripped?");
        return -1;
    }

    return env->RegisterNatives(gFrameSequenceClassInfo.clazz, gMethods, METHOD_COUNT(gMethods));
}
