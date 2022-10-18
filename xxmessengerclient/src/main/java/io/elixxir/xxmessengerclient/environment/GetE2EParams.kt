package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.Data

class GetE2EParams(private val bindings: Bindings) {

    operator fun invoke(): Data {
        return bindings.defaultE2eParams
    }
}