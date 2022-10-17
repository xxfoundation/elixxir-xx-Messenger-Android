package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerIsLoggedIn(private val env: MessengerEnvironment) {

    operator fun invoke(): Boolean {
        return env.ud != null
    }
}