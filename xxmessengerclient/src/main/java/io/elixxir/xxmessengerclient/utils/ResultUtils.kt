package io.elixxir.xxmessengerclient.utils

inline fun <reified T> resultOf(block: () -> T?): Result<T?> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

inline fun <reified T> nonNullResultOf(block: () -> T?): Result<T> {
    return try {
        block()?.let {
            Result.success(it)
        } ?: throw Exception("Null value returned")
    } catch (e: Exception) {
        Result.failure(e)
    }
}