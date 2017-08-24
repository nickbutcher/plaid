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

#ifndef RASTERMILL_COLOR_H
#define RASTERMILL_COLOR_H

#include <sys/types.h>

typedef uint32_t Color8888;

static const Color8888 COLOR_8888_ALPHA_MASK = 0xff000000; // TODO: handle endianness
static const Color8888 TRANSPARENT = 0x0;

// TODO: handle endianness
#define ARGB_TO_COLOR8888(a, r, g, b) \
    ((a) << 24 | (b) << 16 | (g) << 8 | (r))

#endif // RASTERMILL_COLOR_H
