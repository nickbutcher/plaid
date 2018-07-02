package io.plaidapp.base.designernews.data.api.comments

import io.plaidapp.base.data.CoroutinesContextProvider
import kotlinx.coroutines.experimental.Unconfined

fun provideFakeCoroutinesContextProvider(): CoroutinesContextProvider =
        CoroutinesContextProvider(Unconfined, Unconfined)