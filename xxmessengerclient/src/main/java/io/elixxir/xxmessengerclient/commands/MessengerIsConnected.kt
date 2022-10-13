package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerIsConnected(private val env: MessengerEnvironment) {

    operator fun invoke(): Boolean {
        return env.e2e != null
    }
}