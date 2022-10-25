package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerUnregisterForNotifications(private val env: MessengerEnvironment) {

    operator fun invoke() {
        env.e2e?.let {
            env.unregisterForNotifications(it.id)
        }
    }
}