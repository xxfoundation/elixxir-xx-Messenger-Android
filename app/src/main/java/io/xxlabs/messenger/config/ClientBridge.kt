package io.xxlabs.messenger.config

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.bindings.BindingsAdapter

interface ClientBridge {
    val bindings: Bindings
}

class DefaultClient : ClientBridge {
    override val bindings: Bindings = BindingsAdapter()
}