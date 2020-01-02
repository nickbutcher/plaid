/*
 * Copyright 2018 Google LLC.
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

/**
 * Base class for all model types.
 * // TODO - make the item immutable (https://github.com/android/plaid/issues/795)
 */
abstract class PlaidItem(
    @Transient open val id: Long,
    @Transient open val title: String,
    @Transient open var url: String? = null,
    @Transient open val page: Int
) {
    var dataSource: String? = null
    var weight: Float = 0F // used for sorting
    var colspan: Int = 0
}
