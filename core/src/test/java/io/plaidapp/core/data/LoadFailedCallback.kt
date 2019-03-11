/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.core.data

import org.junit.Assert.assertEquals

class LoadFailedCallback : LoadSourceCallback {
    var source: String? = null

    override fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String) {
    }

    override fun loadFailed(source: String) {
        this.source = source
    }

    fun assertLoadFailed(expectedFailedSource: String) {
        assertEquals(expectedFailedSource, source)
    }
}
