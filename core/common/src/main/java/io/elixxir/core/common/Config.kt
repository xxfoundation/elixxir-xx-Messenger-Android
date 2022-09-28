package io.elixxir.core.common

import kotlinx.coroutines.*

interface Config {
    val dispatcher: CoroutineDispatcher
    fun log(message: String, export: Boolean = false)
    fun logException(e: Exception, export: Boolean = true)

    fun newScopeNamed(name: String): CoroutineScope =
        CoroutineScope(
            CoroutineName(name)
                    + Job()
                    + Dispatchers.Default
        )
}

