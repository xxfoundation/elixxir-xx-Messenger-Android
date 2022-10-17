package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.E2eId

class IsRegisteredWithUD(
    private val bindings: Bindings,
) {

    operator fun invoke(e2eId: E2eId): Boolean {
        return bindings.isRegisteredWithUd(e2eId)
    }
}