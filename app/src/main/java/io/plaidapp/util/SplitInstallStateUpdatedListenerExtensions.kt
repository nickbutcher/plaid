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

import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * Performs an action on a [SplitInstallSessionStatus] update.
 */
inline fun SplitInstallSessionState.onStatus(
    crossinline onUnknownError: (errorCode: Int) -> Unit = {},
    crossinline onCanceling: () -> Unit = {},
    crossinline onCanceled: () -> Unit = {},
    crossinline onFailed: () -> Unit = {},
    crossinline onRequiresConfirmation: () -> Unit = {},
    crossinline onPending: () -> Unit = {},
    crossinline onDownloading: (bytesDownloaded: Long, totalBytesToDownload: Long) -> Unit =
        { _,
            _ ->
        },
    crossinline onDownloaded: () -> Unit = {},
    crossinline onInstalling: () -> Unit = {},
    crossinline onInstalled: () -> Unit = {}
) {

    when (status()) {
        SplitInstallSessionStatus.UNKNOWN -> onUnknownError(errorCode())
        SplitInstallSessionStatus.CANCELING -> onCanceling()
        SplitInstallSessionStatus.CANCELED -> onCanceled()
        SplitInstallSessionStatus.FAILED -> onFailed()
        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> onRequiresConfirmation()
        SplitInstallSessionStatus.PENDING -> onPending()
        SplitInstallSessionStatus.DOWNLOADING -> onDownloading(
            bytesDownloaded(),
            totalBytesToDownload()
        )
        SplitInstallSessionStatus.DOWNLOADED -> onDownloaded()
        SplitInstallSessionStatus.INSTALLING -> onInstalling()
        SplitInstallSessionStatus.INSTALLED -> onInstalled()
    }
}

inline fun SplitInstallSessionState.onUnknownError(crossinline action: (errorCode: Int) -> Unit) =
    onStatus(onUnknownError = action)

inline fun SplitInstallSessionState.onCanceling(crossinline action: () -> Unit) =
    onStatus(onCanceling = action)

inline fun SplitInstallSessionState.onCanceled(crossinline action: () -> Unit) =
    onStatus(onCanceled = action)

inline fun SplitInstallSessionState.onFailed(crossinline action: () -> Unit) =
    onStatus(onFailed = action)

inline fun SplitInstallSessionState.onRequiresConfirmation(crossinline action: () -> Unit) =
    onStatus(onRequiresConfirmation = action)

inline fun SplitInstallSessionState.onPending(crossinline action: () -> Unit) =
    onStatus(onPending = action)

inline fun SplitInstallSessionState.onDownloading(
    crossinline action: (
        bytesDownloaded: Long,
        totalBytesToDownload: Long
    ) -> Unit
) =
    onStatus(onDownloading = action)

inline fun SplitInstallSessionState.onDownloaded(crossinline action: () -> Unit) =
    onStatus(onDownloaded = action)

inline fun SplitInstallSessionState.onInstalling(crossinline action: () -> Unit) =
    onStatus(onInstalling = action)

inline fun SplitInstallSessionState.onInstalled(crossinline action: () -> Unit) =
    onStatus(onInstalled = action)

inline fun SplitInstallSessionState.onHappyPath(
    crossinline onPending: () -> Unit = {},
    crossinline onDownloading: (bytesDownloaded: Long, totalBytesToDownload: Long) -> Unit =
        { _,
            _ ->
        },
    crossinline onDownloaded: () -> Unit = {},
    crossinline onInstalling: () -> Unit = {},
    crossinline onInstalled: () -> Unit = {}
) {
    onStatus(
        onPending = onPending,
        onDownloading = onDownloading,
        onDownloaded = onDownloaded,
        onInstalling = onInstalling,
        onInstalled = onInstalled
    )
}
