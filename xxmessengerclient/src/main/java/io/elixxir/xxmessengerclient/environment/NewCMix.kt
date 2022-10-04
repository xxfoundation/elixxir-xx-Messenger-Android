package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.CmixParams
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class NewCMix(
    private val bindings: Bindings,
    private val ndfJson: () -> String,
    private val storageDir: () -> String,
    private val cmixParams: () -> CmixParams,
    private val registrationCode: () -> String,
) {

    operator fun invoke(): Result<Unit> {
        return nonNullResultOf {
            bindings.newCmix(
                ndfJson(),
                storageDir(),
                cmixParams(),
                registrationCode()
            )
        }
    }
}