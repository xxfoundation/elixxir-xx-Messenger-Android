package io.xxlabs.messenger.support.callback

interface MessageDeliveryCallbackWrapper {
    fun onMessageReportReceived(msgId: ByteArray, delivered: Boolean, timeoutMs: Int)
}