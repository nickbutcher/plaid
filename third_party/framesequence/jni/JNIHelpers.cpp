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

#include "JNIHelpers.h"
#include "utils/log.h"

void jniThrowException(JNIEnv* env, const char* className, const char* msg) {
    jclass clazz = env->FindClass(className);
    if (!clazz) {
        ALOGE("Unable to find exception class %s", className);
        /* ClassNotFoundException now pending */
        return;
    }

    if (env->ThrowNew(clazz, msg) != JNI_OK) {
        ALOGE("Failed throwing '%s' '%s'", className, msg);
        /* an exception, most likely OOM, will now be pending */
    }
    env->DeleteLocalRef(clazz);
}
