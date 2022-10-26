package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.AuthEventListener
import io.elixxir.xxmessengerclient.MessengerEnvironment
import java.util.*

class MessengerRegisterAuthCallbacks(
    private val env: MessengerEnvironment
) {

    operator fun invoke(authCallback: AuthEventListener) {
        env.authCallbacks.setAuthCallback(authCallback)
    }
}