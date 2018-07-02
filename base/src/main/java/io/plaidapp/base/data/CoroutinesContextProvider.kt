package io.plaidapp.base.data

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlin.coroutines.experimental.CoroutineContext

data class CoroutinesContextProvider(val main: CoroutineContext = UI,
                                     val io: CoroutineContext = CommonPool)