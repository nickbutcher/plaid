package io.plaidapp.core.data

import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Provide coroutines context.
 */
data class CoroutinesContextProvider(
    val main: CoroutineContext,
    val computation: CoroutineContext,
    val io: CoroutineContext
) {

    @Inject constructor(dispatchers: CoroutinesDispatcherProvider) : this(dispatchers.main, dispatchers.computation, dispatchers.io)
}
