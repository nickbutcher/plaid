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

#ifndef RASTERMILL_REGISTRY_H
#define RASTERMILL_REGISTRY_H

#include "jni.h"
#include <stdint.h>

class FrameSequence;
class Decoder;
class Stream;

struct RegistryEntry {
    int requiredHeaderBytes;
    bool (*checkHeader)(void* header, int header_size);
    FrameSequence* (*createFrameSequence)(Stream* stream);
    Decoder* (*createDecoder)(Stream* stream);
    bool (*acceptsBuffer)();
};

/**
 * Template class for registering subclasses that can produce instances of themselves given a
 * DataStream pointer.
 *
 * The super class / root constructable type only needs to define a single static construction
 * meathod that creates an instance by iterating through all factory methods.
 */
class Registry {
public:
    Registry(const RegistryEntry& entry);

    static const RegistryEntry* Find(Stream* stream);

private:
    RegistryEntry mImpl;
    Registry* mNext;
};

#endif // RASTERMILL_REGISTRY_H
