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

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener

// Update checks

/**
 * Checks for an update and performs an action based on any of the update availability states.
 */
inline fun AppUpdateManager.checkForUpdate(
    crossinline noUpdateAvailable: (info: AppUpdateInfo) -> Unit = {},
    crossinline updateInProgress: (info: AppUpdateInfo) -> Unit = {},
    crossinline flexibleUpdate: (info: AppUpdateInfo) -> Unit = {},
    crossinline immediateUpdate: (info: AppUpdateInfo) -> Unit = {}
) {
    val listener = OnSuccessListener<AppUpdateInfo> { info ->
        with(info) {
            when (updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    if (isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        flexibleUpdate(this)
                    } else if (isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        immediateUpdate(this)
                    }
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> updateInProgress(this)
                else -> noUpdateAvailable(this)
            }
        }
    }
    appUpdateInfo.addOnSuccessListener(listener)
}

inline fun AppUpdateManager.doOnImmediateUpdate(crossinline action: (info: AppUpdateInfo) -> Unit) =
    checkForUpdate(immediateUpdate = action)

inline fun AppUpdateManager.doOnFlexibleUpdate(crossinline action: (info: AppUpdateInfo) -> Unit) =
    checkForUpdate(flexibleUpdate = action)

inline fun AppUpdateManager.doOnNoUpdate(crossinline action: (info: AppUpdateInfo) -> Unit) =
    checkForUpdate(noUpdateAvailable = action)

inline fun AppUpdateManager.doOnUpdateInProgress(
    crossinline action: (info: AppUpdateInfo) -> Unit
) = checkForUpdate(updateInProgress = action)

inline fun AppUpdateManager.doOnAppUpdateInfoRetrieved(
    crossinline action: (info: AppUpdateInfo) -> Unit
) = checkForUpdate(action, action, action)

// Update the app

/**
 * Update the app for an update of type [AppUpdateType.FLEXIBLE].
 */
fun AppUpdateManager.updateFlexibly(activity: Activity, resultCode: Int) {
    doOnFlexibleUpdate {
        startUpdateFlowForResult(
            it,
            AppUpdateType.IMMEDIATE,
            activity,
            resultCode
        )
    }
}

/**
 * Update the app for an update of type [AppUpdateType.IMMEDIATE].
 */
fun AppUpdateManager.updateImmediately(activity: Activity, resultCode: Int) {
    doOnImmediateUpdate {
        startUpdateFlowForResult(
            it,
            AppUpdateType.IMMEDIATE,
            activity,
            resultCode
        )
    }
}

/**
 * Update the app for a given update type.
 *
 * @param activity The activity that performs the update.
 * @param resultCode The result code to use within your activity's onActivityResult.
 * @param type The type of update to perform.
 */
fun AppUpdateManager.update(
    activity: Activity,
    resultCode: Int,
    @AppUpdateType type: Int
) {
    doOnAppUpdateInfoRetrieved {
        if (it.isUpdateTypeAllowed(type)) {
            if (it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                updateFlexibly(activity, resultCode)
            } else if (it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                updateImmediately(activity, resultCode)
            }
        }
        // TODO handle update type not allowed flow.
    }
}

// Install state handling

/**
 * Perform an action on any provided install state.
 */
inline fun AppUpdateManager.doOnInstallState(
    crossinline onUnknown: (errorCode: Int) -> Unit = {},
    crossinline onCanceled: (errorCode: Int) -> Unit = {},
    crossinline onFailed: (errorCode: Int) -> Unit = {},
    crossinline onRequiresUiIntent: () -> Unit = {},
    crossinline onPending: () -> Unit = {},
    crossinline onDownloading: () -> Unit = {},
    crossinline onDownloaded: () -> Unit = {},
    crossinline onInstalling: () -> Unit = {},
    crossinline onInstalled: () -> Unit = {}
): InstallStateUpdatedListener {
    return InstallStateUpdatedListener {
        it.onStatus(
            onUnknown = onUnknown,
            onCanceled = onCanceled,
            onFailed = onFailed,
            onRequiresUiIntent = onRequiresUiIntent,
            onPending = onPending,
            onDownloading = onDownloading,
            onDownloaded = onDownloaded,
            onInstalling = onInstalling,
            onInstalled = onInstalled
        )
    }
}

inline fun AppUpdateManager.onInstallStateUnknown(crossinline onUnknown: (errorCode: Int) -> Unit) =
    doOnInstallState(onUnknown = onUnknown)

inline fun AppUpdateManager.onInstallStateCanceled(
    crossinline onCanceled: (errorCode: Int) -> Unit
) =
    doOnInstallState(onCanceled = onCanceled)

inline fun AppUpdateManager.onInstallStateFailed(crossinline onFailed: (errorCode: Int) -> Unit) =
    doOnInstallState(onFailed = onFailed)

inline fun AppUpdateManager.onInstallStateRequiresUiIntent(
    crossinline onRequiresUiIntent: () -> Unit
) =
    doOnInstallState(onRequiresUiIntent = onRequiresUiIntent)

inline fun AppUpdateManager.onInstallStatePending(crossinline onPending: () -> Unit) =
    doOnInstallState(onPending = onPending)

inline fun AppUpdateManager.onInstallStateDownloading(
    crossinline onDownloading: () -> Unit
) =
    doOnInstallState(onDownloading = onDownloading)

inline fun AppUpdateManager.onInstallStateDownloaded(
    crossinline onDownloaded: () -> Unit
) =
    doOnInstallState(onDownloaded = onDownloaded)

inline fun AppUpdateManager.onInstallStateRequiresInstalling(
    crossinline onInstalling: () -> Unit
) =
    doOnInstallState(onInstalling = onInstalling)

inline fun AppUpdateManager.onInstallStateInstalled(
    crossinline onInstalled: () -> Unit
) =
    doOnInstallState(onInstalled = onInstalled)

inline fun AppUpdateManager.onActivityResult(
    resultCode: Int,
    accepted: () -> Unit,
    canceled: () -> Unit,
    failed: () -> Unit
) {
    when (resultCode) {
        Activity.RESULT_OK -> accepted()
        Activity.RESULT_CANCELED -> canceled()
        ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> failed()
    }
}
