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

import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.InstallStatus

/**
 * Performs an action on a given [InstallStatus].
 */
inline fun InstallState.onStatus(
    crossinline onUnknown: (errorCode: Int) -> Unit = {},
    crossinline onCanceled: (errorCode: Int) -> Unit = {},
    crossinline onFailed: (errorCode: Int) -> Unit = {},
    crossinline onRequiresUiIntent: () -> Unit = {},
    crossinline onPending: () -> Unit = {},
    crossinline onDownloading: () -> Unit = {},
    crossinline onDownloaded: () -> Unit = {},
    crossinline onInstalling: () -> Unit = {},
    crossinline onInstalled: () -> Unit = {}
) {
    when (installStatus()) {
        InstallStatus.UNKNOWN -> onUnknown(installErrorCode())
        InstallStatus.CANCELED -> onCanceled(installErrorCode())
        InstallStatus.FAILED -> onFailed(installErrorCode())
        InstallStatus.REQUIRES_UI_INTENT -> onRequiresUiIntent()
        InstallStatus.PENDING -> onPending()
        InstallStatus.DOWNLOADING -> onDownloading()
        InstallStatus.DOWNLOADED -> onDownloaded()
        InstallStatus.INSTALLING -> onInstalling()
        InstallStatus.INSTALLED -> onInstalled()
    }
}

inline fun InstallState.onUnknownError(crossinline action: (errorCode: Int) -> Unit) {
    onStatus(onUnknown = action)
}

inline fun InstallState.onCanceled(crossinline action: (errorCode: Int) -> Unit) {
    onStatus(onCanceled = action)
}

inline fun InstallState.onFailed(crossinline action: (errorCode: Int) -> Unit) {
    onStatus(onFailed = action)
}

inline fun InstallState.onRequiresUiIntent(crossinline action: () -> Unit) {
    onStatus(onRequiresUiIntent = action)
}

inline fun InstallState.onPending(crossinline action: () -> Unit) {
    onStatus(onPending = action)
}

inline fun InstallState.onDownloading(crossinline action: () -> Unit) {
    onStatus(onDownloading = action)
}

inline fun InstallState.onDownloaded(crossinline action: () -> Unit) {
    onStatus(onDownloaded = action)
}

inline fun InstallState.onInstalling(crossinline action: () -> Unit) {
    onStatus(onInstalling = action)
}

inline fun InstallState.onInstalled(crossinline action: () -> Unit) {
    onStatus(onInstalled = action)
}
