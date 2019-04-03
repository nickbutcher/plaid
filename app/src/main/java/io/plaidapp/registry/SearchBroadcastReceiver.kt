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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Extend this receiver to enable hooking into Plaid's #SearchDataSourceFactoriesRegistry.
 */
abstract class SearchBroadcastReceiver(
    private val target: Class<out FactoryRegistrationService>
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO handle intent
        if (context != null) {
            val startRegistrationService = Intent(context, target)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startRegistrationService)
            } else {
                context.startService(startRegistrationService)
            }
        }
    }

    companion object {
        const val ACTION = "io.plaidapp.register.SEARCH_FACTORY"
    }
}
