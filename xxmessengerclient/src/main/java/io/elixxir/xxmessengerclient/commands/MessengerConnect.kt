package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.AuthEventListener
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException
import java.util.*

class MessengerConnect(private val env: MessengerEnvironment) {

    operator fun invoke() {
        env.authCallbacks.authCallbacks.apply {
            if (isEmpty()) put(UUID.randomUUID(), authListener)
        }
        
        val cMix = env.cMix ?: throw MessengerException.NotLoaded("Cmix")

        env.e2e = env.login(
            cMixId = cMix.id,
            authCallbacks = env.authCallbacks.authCallbacks.values.first(),
            receptionId = cMix.makeReceptionIdentity(),
            e2eParams = env.getE2EParams()
        ).getOrThrow()
    }
}

private val authListener: AuthEventListener by lazy {
    object : AuthEventListener {
        override fun onConfirm(
            contact: ByteArray?,
            receptionId: ByteArray?,
            ephemeralId: Long,
            roundId: Long
        ) {

        }

        override fun onRequest(
            contact: ByteArray?,
            receptionId: ByteArray?,
            ephemeralId: Long,
            roundId: Long
        ) {

        }

        override fun onReset(
            contact: ByteArray?,
            receptionId: ByteArray?,
            ephemeralId: Long,
            roundId: Long
        ) {

        }
    }
}