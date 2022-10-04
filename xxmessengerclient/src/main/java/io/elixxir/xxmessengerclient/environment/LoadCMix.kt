package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.utils.CmixParams
import io.elixxir.xxclient.utils.Password
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class LoadCMix(
    private val bindings: Bindings,
    private val storageDir: () -> String,
    private val password: () -> Password,
    private val cMixParams: () -> CmixParams,
) {

    operator fun invoke(): Result<CMix> {
        return nonNullResultOf {
            bindings.loadCmix(
                storageDir(),
                password(),
                cMixParams()
            )
        }
    }
}