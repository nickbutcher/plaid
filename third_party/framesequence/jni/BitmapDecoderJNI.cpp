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

#define LOG_TAG "FancyDecoding"

#include <android/bitmap.h>
#include <stdlib.h>
#include <stdio.h>
#include "FrameSequenceJNI.h"
#include "JNIHelpers.h"
#include "Stream.h"
#include "utils/log.h"

void throwException(JNIEnv* env, const char* error) {
    jclass clazz = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(clazz, error);
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    if (FrameSequence_OnLoad(env)) {
        ALOGE("Failed to load FrameSequence");
        return -1;
    }
    if (JavaStream_OnLoad(env)) {
        ALOGE("Failed to load JavaStream");
        return -1;
    }

    return JNI_VERSION_1_6;
}
