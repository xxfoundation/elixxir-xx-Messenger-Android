package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerRegisterForNotifications(private val env: MessengerEnvironment) {

    operator fun invoke(token: String) {
        env.e2e?.let {
            env.registerForNotifications(it.id, token)
        }
    }
}