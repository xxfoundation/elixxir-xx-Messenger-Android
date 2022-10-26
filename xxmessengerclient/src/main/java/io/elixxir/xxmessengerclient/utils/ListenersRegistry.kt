package io.elixxir.xxmessengerclient.utils

import io.elixxir.xxclient.callbacks.MessageListener
import io.elixxir.xxclient.models.Message

class ListenersRegistry {
    private var messageListener: MessageListener? = null
    private var messageCache: MessageCache? = null

    fun getListener(): MessageListener {
        return messageListener ?: messageCache ?: run {
            MessageCache().apply {
                messageCache = this
            }
        }
    }

    fun setListener(listener: MessageListener) {
        messageCache?.setListener(listener) ?: run {
            messageListener = listener
        }
    }
}

private class MessageCache : MessageListener {

    private var listener: MessageListener? = null
    private val messageCache = mutableListOf<Message>()

    override val name: String = "MessageCache"

    fun setListener(listener: MessageListener) {
        this.listener = listener
        sendMessages(listener)
        clearCache()
    }

    private fun sendMessages(listener: MessageListener) {
        messageCache.forEach {
            listener.onMessageReceived(it)
        }
    }

    private fun clearCache() {
        messageCache.clear()
    }

    override fun onMessageReceived(message: Message) {
        listener?.onMessageReceived(message) ?: run {
            messageCache.add(message)
        }
    }
}