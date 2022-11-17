package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerIsRegistered(private val env: MessengerEnvironment) {

    operator fun invoke(): Boolean {
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
        return env.isRegisteredWithUD(e2e.id)
    }
}