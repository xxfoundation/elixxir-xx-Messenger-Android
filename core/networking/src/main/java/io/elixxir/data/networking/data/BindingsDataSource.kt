package io.elixxir.data.networking.data

import io.elixxir.data.networking.BindingsRepository
import io.elixxir.xxclient.bindings.BindingsAdapter
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.utils.CmixParams
import io.elixxir.xxclient.utils.Password

/**
 * A [BindingsAdapter] subclass that exposes a cached [CMix] reference.
 */
internal class BindingsDataSource : BindingsAdapter(), BindingsRepository {
    private var cMix: CMix? = null

    override fun getCMix(): CMix? = cMix

    override fun loadCmix(
        sessionFileDirectory: String,
        sessionPassword: Password,
        cmixParams: CmixParams
    ): CMix {
        return cMix ?: super.loadCmix(sessionFileDirectory, sessionPassword, cmixParams).apply {
            cMix = this
        }
    }
}