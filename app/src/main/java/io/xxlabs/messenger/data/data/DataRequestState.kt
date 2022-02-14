package io.xxlabs.messenger.data.data

sealed class DataRequestState<T> {
    class Start<T>(var data: String? = null) : DataRequestState<T>()
    class Success<T>(var data: T) : DataRequestState<T>()
    class Completed<T> : DataRequestState<T>()
    class Error<T>(val error: Throwable) : DataRequestState<T>()
}