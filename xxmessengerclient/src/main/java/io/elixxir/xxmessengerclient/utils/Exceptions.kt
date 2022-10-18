package io.elixxir.xxmessengerclient.utils

sealed class MessengerException(message: String?) : Exception(message) {

    class NotLoaded(
        componentName: String? = null
    ) : MessengerException(
        componentName?.let { "$it component is required and hasn't been loaded."}
    )

    class TimedOut : MessengerException("The network operation timed out.")
}