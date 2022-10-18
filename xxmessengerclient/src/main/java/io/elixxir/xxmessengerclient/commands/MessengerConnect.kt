package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerConnect(private val env: MessengerEnvironment) {

    operator fun invoke() {
        val cMix = env.cMix ?: throw MessengerException.NotLoaded("Cmix")

        env.e2e = env.login(
            cMixId = cMix.id,
            authCallbacks = env.authCallbacks.authCallbacks.values.first(),
            receptionId = cMix.makeReceptionIdentity(),
            e2eParams = env.getE2EParams()
        ).getOrThrow()
    }
}