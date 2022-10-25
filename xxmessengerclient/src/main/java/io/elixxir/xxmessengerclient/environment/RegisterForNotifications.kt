package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.E2eId

class RegisterForNotifications(private val bindings: Bindings) {

    operator fun invoke(e2eId: E2eId, token: String) {
        bindings.registerForNotifications(e2eId, token)
    }
}