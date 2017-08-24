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

#include "Registry.h"

#include "Stream.h"

static Registry* gHead = 0;
static int gHeaderBytesRequired = 0;

Registry::Registry(const RegistryEntry& entry) {
    mImpl = entry;

    mNext = gHead;
    gHead = this;

    if (gHeaderBytesRequired < entry.requiredHeaderBytes) {
        gHeaderBytesRequired = entry.requiredHeaderBytes;
    }
}

const RegistryEntry* Registry::Find(Stream* stream) {
    Registry* registry = gHead;

    if (stream->getRawBuffer() != NULL) {
        while (registry) {
            if (registry->mImpl.acceptsBuffer()) {
                return &(registry->mImpl);
            }
            registry = registry->mNext;
        }
    } else {
        int headerSize = gHeaderBytesRequired;
        char header[headerSize];
        headerSize = stream->peek(header, headerSize);
        while (registry) {
            if (headerSize >= registry->mImpl.requiredHeaderBytes
                    && registry->mImpl.checkHeader(header, headerSize)) {
                return &(registry->mImpl);
            }
            registry = registry->mNext;
        }
    }
    return 0;
}

