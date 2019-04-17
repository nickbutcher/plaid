/*
 * Copyright 2019 Google, Inc.
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

package io.plaidapp.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.Result
import io.plaidapp.core.feed.DataSourcesRegistry
import io.plaidapp.core.interfaces.PlaidDataSource
import io.plaidapp.core.ui.getPlaidItemsForDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlin.coroutines.coroutineContext

class LoadFeedUseCase(
    private val registry: DataSourcesRegistry
) {

    private val dataSources = registry.dataSources

    private val _searchResult = MutableLiveData<List<PlaidItem>>()
    val searchResult: LiveData<List<PlaidItem>>
        get() = _searchResult

    suspend operator fun invoke() {
        dataSources.value?.apply { loadMore(this) }
    }

    private suspend fun loadMore(dataSources: Set<PlaidDataSource>) {
        val job = SupervisorJob(coroutineContext[Job])
        val scope = CoroutineScope(job)
        val deferredJobs = mutableListOf<Deferred<Unit>>()

        dataSources.forEach {
            deferredJobs.add(scope.async {
                val result = it.loadMore()
                if (result is Result.Success) {
                    val oldItems = _searchResult.value.orEmpty().toMutableList()
                    val searchResult = getPlaidItemsForDisplay(oldItems, result.data)
                    _searchResult.postValue(searchResult)
                }
            })
        }
        deferredJobs.forEach { it.await() }
    }
}
