package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerIsListeningForMessages(private val env: MessengerEnvironment) {

    operator fun invoke(): Boolean {
        return env.isListeningForMessages
    }
}