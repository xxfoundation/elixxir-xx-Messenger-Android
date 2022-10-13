package io.elixxir.xxmessengerclient.utils

sealed class MessengerException(message: String?) : Exception(message) {

    class NotLoaded : MessengerException(null)
    class TimedOut : MessengerException(null)
}