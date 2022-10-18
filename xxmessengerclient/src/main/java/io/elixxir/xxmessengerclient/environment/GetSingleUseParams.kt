package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.Data
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class GetSingleUseParams(private val bindings: Bindings) {

    operator fun invoke(): Data {
        return bindings.defaultSingleUseParams
    }
}