/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.core.producthunt.data

import io.plaidapp.core.R
import io.plaidapp.core.data.SourceItem

data class ProductHuntSourceItem(override val name: String) : SourceItem(
    SOURCE_PRODUCT_HUNT,
    SOURCE_PRODUCT_HUNT,
    500,
    name,
    R.drawable.ic_product_hunt,
    false
) {
    companion object {
        const val SOURCE_PRODUCT_HUNT = "SOURCE_PRODUCT_HUNT"
    }
}
