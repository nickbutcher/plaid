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

package io.plaidapp.core.producthunt.data.api

import io.plaidapp.core.data.Result
import io.plaidapp.core.producthunt.data.ProductHuntRemoteDataSource
import io.plaidapp.core.producthunt.data.api.model.GetPostsResponse
import io.plaidapp.core.producthunt.data.responseDataSuccess
import io.plaidapp.core.producthunt.data.responseError
import io.plaidapp.core.producthunt.data.responseSuccess
import io.plaidapp.test.shared.provideFakeCoroutinesDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests for [ProductHuntRepository].
 */
@ExperimentalCoroutinesApi
class ProductHuntRepositoryTest {

    private val coroutinesDispatcherProvider = provideFakeCoroutinesDispatcherProvider()

    @Test
    fun loadPosts_success() = runBlocking {
        // Given a repository that got data successful
        val repository = repositoryWithData(responseSuccess)

        // When loading data
        val data = repository.loadPosts(1)

        // Then the correct data was loaded
        assertEquals(Result.Success(responseDataSuccess), data)
    }

    @Test
    fun loadPosts_error() = runBlocking {
        // Given a repository that got data with error
        val repository = repositoryWithData(responseError)

        // When loading data
        val data = repository.loadPosts(1)

        // Then error is returned
        assertTrue(data is Result.Error)
    }

    private fun repositoryWithData(result: Response<GetPostsResponse>): ProductHuntRepository {
        val serviceFake = object : FakeProductHuntService() {
            override fun getPostsResponse() = result
        }
        val remoteDataSource = ProductHuntRemoteDataSource(serviceFake)
        return ProductHuntRepository(remoteDataSource, coroutinesDispatcherProvider)
    }
}
