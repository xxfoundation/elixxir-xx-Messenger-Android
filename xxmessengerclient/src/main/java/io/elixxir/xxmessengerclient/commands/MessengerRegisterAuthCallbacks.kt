package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.AuthEventListener
import io.elixxir.xxmessengerclient.MessengerEnvironment
import java.util.*

class MessengerRegisterAuthCallbacks(
    private val env: MessengerEnvironment
) {

    operator fun invoke(authCallback: AuthEventListener) {
        env.authCallbacks.authCallbacks.run {
            if (isEmpty()) {
                put(UUID.randomUUID(), authCallback)
            } else {
                set(env.authCallbacks.authCallbacks.keys.first(), authCallback)
            }
        }
    }
}