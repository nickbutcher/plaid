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

package io.plaidapp.core.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.SourceItem

/**
 * Class that allows loading data based on a [SourceItem].
 * Implementations of this class will handle how the data is retrieved and how pages are handled
 */
abstract class PlaidDataSource(val sourceItem: SourceItem) {

    protected val _items = MutableLiveData<List<PlaidItem>>()
    val items: LiveData<List<PlaidItem>>
        get() = _items

    /**
     * Load more data from this source
     */
    abstract suspend fun loadMore()
}
