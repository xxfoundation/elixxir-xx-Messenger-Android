package io.xxlabs.messenger.data.data

sealed class SimpleRequestState<T> {
    class Success<T>(val value: T) : SimpleRequestState<T>()
    class Error<T>(val error: Throwable? = null) : SimpleRequestState<T>()
    class Completed<T> : SimpleRequestState<T>()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        if (this is Success && other is Success<*>) {
            return value == other.value
        } else if (this is Error && other is Error<*>) {
            return error == other.error
        } else if (this is Completed && other is Completed<*>) {
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}