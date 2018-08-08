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
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.util.event.Event
import io.plaidapp.core.util.exhaustive
import io.plaidapp.dribbble.domain.CreateShotUiModelUseCase
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import io.plaidapp.dribbble.domain.ShareShotInfo
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * View model for [ShotActivity].
 */
class ShotViewModel(
    shotId: Long,
    shotsRepository: ShotsRepository,
    private val createShotUiModel: CreateShotUiModelUseCase,
    private val getShareShotInfo: GetShareShotInfoUseCase,
    private val contextProvider: CoroutinesContextProvider
) : ViewModel() {

    private val shotUiModel = MutableLiveData<ShotUiModel>()

    private var shareShotJob: Job? = null
    private var createUiModelJob: Job? = null

    private val _openLink = MutableLiveData<Event<String>>()
    val openLink: LiveData<Event<String>>
        get() = _openLink

    private val _shareShot = MutableLiveData<Event<ShareShotInfo>>()
    val shareShot: LiveData<Event<ShareShotInfo>>
        get() = _shareShot

    init {
        val result = shotsRepository.getShot(shotId)
        when (result) {
            is Result.Success -> shotUiModel.value = result.data.toShotUiModelSync()
            is Result.Error -> throw IllegalStateException(
                "Could not retrieve shot $shotId",
                result.exception
            )
        }.exhaustive
    }

    fun getShotUiModel(styler: ShotStyler): LiveData<ShotUiModel> {
        // only allow one job at a time
        if (createUiModelJob == null) {
            createUiModelJob = launchCreateShotUiModel(styler)
        }
        return shotUiModel
    }

    fun shareShotRequested() {
        shareShotJob?.cancel()
        shareShotJob = launchShare()
    }

    fun viewShotRequested() {
        shotUiModel.value?.let { model ->
            _openLink.value = Event(model.url)
        }
    }

    fun getShotId(): Long {
        return shotUiModel.value?.id ?: -1L
    }

    fun getAssistWebUrl(): String {
        return shotUiModel.value?.url.orEmpty()
    }

    override fun onCleared() {
        shareShotJob?.cancel()
        createUiModelJob?.cancel()
    }

    private fun launchCreateShotUiModel(styler: ShotStyler) = launch(contextProvider.io) {
        shotUiModel.value?.let { model ->
            val newModel = createShotUiModel(model, styler).await()
            withContext(contextProvider.main) {
                shotUiModel.value = newModel
            }
        }
    }

    private fun launchShare() = launch(contextProvider.io) {
        shotUiModel.value?.let { model ->
            val shareInfo = getShareShotInfo(model)
            withContext(contextProvider.main) {
                _shareShot.value = Event(shareInfo)
            }
        }
    }
}
