package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.utils.CmixParams
import io.elixxir.xxclient.utils.Password
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class LoadCMix(private val bindings: Bindings) {

    operator fun invoke(
        storageDir: String,
        password: Password,
        cMixParams: CmixParams,
    ): Result<CMix> {
        return nonNullResultOf {
            bindings.loadCmix(
                storageDir,
                password,
                cMixParams
            )
        }
    }
}