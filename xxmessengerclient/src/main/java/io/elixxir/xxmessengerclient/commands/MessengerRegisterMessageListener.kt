package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.MessageListener
import io.elixxir.xxmessengerclient.MessengerEnvironment
import java.util.*

class MessengerRegisterMessageListener(private val env: MessengerEnvironment) {

    operator fun invoke(listener: MessageListener) {
        env.messageListeners.setListener(listener)
    }
}