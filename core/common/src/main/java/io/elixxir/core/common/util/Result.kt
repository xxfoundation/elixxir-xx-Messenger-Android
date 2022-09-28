package io.elixxir.core.common.util

import io.elixxir.core.common.Config

/**
 * Convenience method for returning a Result<T> wrapped in a try/catch.
 */
inline fun <T> Config.resultOf(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        logException(e)
        Result.failure(e)
    }
}
