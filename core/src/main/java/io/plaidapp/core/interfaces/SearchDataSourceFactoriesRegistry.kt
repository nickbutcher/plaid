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

package io.plaidapp.core.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

private const val designerNewsSearchDataSourceFactoryProviderClassName =
    "io.plaidapp.designernews.domain.search.DesignerNewsSearchFactoryProvider"

// TODO add dribbble as well
private val factoryClassNames =
    listOf(designerNewsSearchDataSourceFactoryProviderClassName)

class SearchDataSourceFactoriesRegistry @Inject constructor() {

    private val _dataSourceFactories =
        MutableLiveData<List<SearchDataSourceFactory>>(getAlreadyAvailableDataSourceFactories())

    val dataSourceFactories: LiveData<List<SearchDataSourceFactory>>
        get() = _dataSourceFactories

    fun add(dataSourceFactory: SearchDataSourceFactory) {
        val existingDataSources = _dataSourceFactories.value.orEmpty().toMutableList()
        existingDataSources.add(dataSourceFactory)
        _dataSourceFactories.postValue(existingDataSources)
    }

    fun remove(dataSourceFactory: SearchDataSourceFactory) {
        val existingDataSources = _dataSourceFactories.value.orEmpty().toMutableList()
        existingDataSources.remove(dataSourceFactory)
        _dataSourceFactories.postValue(existingDataSources)
    }

    private fun getAlreadyAvailableDataSourceFactories(): List<SearchDataSourceFactory> {
        return factoryClassNames.map {
            val clazz = Class.forName(it)
            val instance = clazz.newInstance() as SearchFactoryProvider
            instance.getFactory()
        }
    }
}
