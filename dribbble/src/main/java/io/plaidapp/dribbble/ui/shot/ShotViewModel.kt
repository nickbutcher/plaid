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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.util.event.Event
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import io.plaidapp.dribbble.domain.ShareShotInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * View model for [ShotActivity].
 */
class ShotViewModel @Inject constructor(
    shotId: Long,
    shotsRepository: ShotsRepository,
    private val getShareShotInfoUseCase: GetShareShotInfoUseCase,
    private val contextProvider: CoroutinesContextProvider
) : ViewModel() {

    val shot: Shot

    init {
        val result = shotsRepository.getShot(shotId)
        if (result is Result.Success) {
            shot = result.data
        } else {
            // TODO re-throw Error.exception once Loading state removed.
            throw IllegalStateException("Could not retrieve shot $shotId")
        }
    }

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
        _openLink.value = Event(shot.htmlUrl)
    }

    override fun onCleared() {
        shareShotJob?.cancel()
    }

    private fun launchShare() = launch(contextProvider.io) {
        val shareInfo = getShareShotInfoUseCase(shot)
        _shareShot.postValue(Event(shareInfo))
    }
}
