package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.Password
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class NewCMix(private val bindings: Bindings) {

    operator fun invoke(
        ndfJson: String,
        storageDir: String,
        password: Password,
        registrationCode: String,
    ): Result<Unit> {
        return nonNullResultOf {
            bindings.newCmix(
                ndfJson,
                storageDir,
                password,
                registrationCode
            )
        }
    }
}