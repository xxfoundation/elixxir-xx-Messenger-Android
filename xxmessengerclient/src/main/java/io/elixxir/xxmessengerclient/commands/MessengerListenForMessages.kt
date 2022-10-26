package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.MessageType
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerListenForMessages(private val env: MessengerEnvironment) {

    operator fun invoke() {
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
        try {
            e2e.registerListener(
                senderId = byteArrayOf(),
                messageType = MessageType.XxMessage,
                e2eListener = env.messageListeners.getListener()
            )
            env.isListeningForMessages = true
        } catch (e: Exception) {
            env.isListeningForMessages = false
            throw e
        }
    }
}