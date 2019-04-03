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

package io.plaidapp.registry

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import io.plaidapp.core.interfaces.SearchDataSourceFactory
import io.plaidapp.ui.PlaidApplication

/**
 * Enable registering #SearchDataSourceFactory.
 */
abstract class FactoryRegistrationService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO create an actual notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                1,
                Notification.Builder(this, "Plaid").setContentTitle(
                    "Registering Search Service"
                ).build()
            )
        }
        registerFactory()
        return super.onStartCommand(intent, flags, startId)
    }

    abstract fun getFactory(): SearchDataSourceFactory

    private fun registerFactory() {
        PlaidApplication.coreComponent(this).registry().add(getFactory())
        stopSelf()
    }
}
