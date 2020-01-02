/*
 * Copyright 2018 Google LLC.
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
import androidx.lifecycle.viewModelScope
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.util.event.Event
import io.plaidapp.dribbble.domain.CreateShotUiModelUseCase
import io.plaidapp.dribbble.domain.GetShareShotInfoUseCase
import io.plaidapp.dribbble.domain.ShareShotInfo
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * View model for [ShotActivity].
 */
class ShotViewModel @Inject constructor(
    shotId: Long,
    shotsRepository: ShotsRepository,
    private val createShotUiModel: CreateShotUiModelUseCase,
    private val getShareShotInfo: GetShareShotInfoUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    private val _shotUiModel = MutableLiveData<ShotUiModel>()
    val shotUiModel: LiveData<ShotUiModel>
        get() = _shotUiModel

    private val _openLink = MutableLiveData<Event<String>>()
    val openLink: LiveData<Event<String>>
        get() = _openLink

    private val _shareShot = MutableLiveData<Event<ShareShotInfo>>()
    val shareShot: LiveData<Event<ShareShotInfo>>
        get() = _shareShot

    init {
        val result = shotsRepository.getShot(shotId)
        if (result is Result.Success) {
            _shotUiModel.value = result.data.toShotUiModel()
            processUiModel(result.data)
        } else {
            // TODO re-throw Error.exception once Loading state removed.
            throw IllegalStateException("Could not retrieve shot $shotId")
        }
    }

    fun shareShotRequested() {
        _shotUiModel.value?.let { model ->
            viewModelScope.launch(dispatcherProvider.io) {
                val shareInfo = getShareShotInfo(model)
                _shareShot.postValue(Event(shareInfo))
            }
        }
    }

    fun viewShotRequested() {
        _shotUiModel.value?.let { model ->
            _openLink.value = Event(model.url)
        }
    }

    fun getAssistWebUrl(): String {
        return shotUiModel.value?.url.orEmpty()
    }

    fun getShotId(): Long {
        return shotUiModel.value?.id ?: -1L
    }

    private fun processUiModel(shot: Shot) {
        viewModelScope.launch(dispatcherProvider.main) {
            val uiModel = createShotUiModel(shot)
            _shotUiModel.value = uiModel
        }
    }
}
