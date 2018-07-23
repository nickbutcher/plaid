/*
 * Copyright 2018 Google, Inc.
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

package io.plaidapp.dribbble.ui.shot

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.util.event.Event
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import io.plaidapp.dribbble.domain.ShareShotInfo
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

/**
 * View model for [DribbbleShotActivity].
 */
class DribbbleShotViewModel(
    private val contextProvider: CoroutinesContextProvider,
    private val getShareShotInfoUseCase: GetShareShotInfoUseCase
) : ViewModel() {

    var shot: Shot? = null // TODO retrieve this from the repo
    private var shareShotJob: Job? = null

    private val _openLink = MutableLiveData<Event<String>>()
    val openLink: LiveData<Event<String>>
        get() = _openLink

    private val _shareShot = MutableLiveData<Event<ShareShotInfo>>()
    val shareShot: LiveData<Event<ShareShotInfo>>
        get() = _shareShot

    fun shareShotRequested() {
        shareShotJob?.cancel()
        shareShotJob = launchShare()
    }

    fun viewShotRequested() {
        val shot = shot ?: return
        _openLink.value = Event(shot.htmlUrl)
    }

    override fun onCleared() {
        shareShotJob?.cancel()
    }

    private fun launchShare() = launch(contextProvider.io) {
        val shareInfo = getShareShotInfoUseCase(shot!!)
        _shareShot.postValue(Event(shareInfo))
    }
}
