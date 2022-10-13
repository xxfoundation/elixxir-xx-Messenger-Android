package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerIsLoaded(private val env: MessengerEnvironment) {

    operator fun invoke(): Boolean {
        return env.cMix != null
    }
}