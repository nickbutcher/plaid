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

package io.plaidapp.util

import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting

/**
 * A [WorkerFactory] which delegates to other factories. Factories can register themselves
 * as delegates, and they will be invoked in order until a delegated factory returns a
 * non-null [ListenableWorker] instance.
 */
class PlaidWorkerFactory : WorkerFactory() {

    private val TAG by lazy { PlaidWorkerFactory::class.java.simpleName }

    @VisibleForTesting
    var factories: MutableMap<Class<WorkerFactory>, WorkerFactory>
        private set

    /**
     * Creates a new instance of the [DelegatingWorkerFactory].
     */
    init {
        factories = LinkedHashMap()
    }

    /**
     * Adds a [WorkerFactory] to the list of delegates.
     *
     * @param workerFactory The [WorkerFactory] instance.
     */
    fun addFactory(workerFactory: WorkerFactory) {
        factories.put(workerFactory.javaClass, workerFactory)
    }

    override fun createWorker(
        context: Context,
        workerClass: String,
        parameters: WorkerParameters
    ): ListenableWorker? {

        for (factory in factories.values) {
            try {
                val worker = factory.createWorker(
                        context, workerClass, parameters)
                if (worker != null) {
                    return worker
                }
            } catch (throwable: Throwable) {
                val message = String.format("Unable to instantiate a ListenableWorker (%s)", workerClass)
                Log.e(TAG, message, throwable)
                throw throwable
            }
        }
        // If none of the delegates can instantiate a ListenableWorker return null
        // so we can fallback to the default factory which is based on reflection.
        return null
    }
}
