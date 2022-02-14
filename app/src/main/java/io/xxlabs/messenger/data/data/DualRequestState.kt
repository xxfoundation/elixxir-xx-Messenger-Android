package io.xxlabs.messenger.data.data

sealed class DualRequestState<T> {
    class Success<T>(val value: T) : DualRequestState<T>()
    class Completed<T> : DualRequestState<T>()
}