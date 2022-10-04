package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.Data

class GenerateSecret(
    private val bindings: Bindings,
    private val byteLength: Long = 32
) {

    operator fun invoke(): Data {
        return bindings.generateSecret(byteLength)
    }
}