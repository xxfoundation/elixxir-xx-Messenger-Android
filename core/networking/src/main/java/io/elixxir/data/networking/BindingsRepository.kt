package io.elixxir.data.networking

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.cmix.CMix

/**
 * A [Bindings] subclass that exposes a cached [CMix] reference.
 */
interface BindingsRepository : Bindings {
    fun getCMix(): CMix?
}