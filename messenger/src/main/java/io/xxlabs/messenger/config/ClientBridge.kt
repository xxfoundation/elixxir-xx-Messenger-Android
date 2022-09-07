package io.xxlabs.messenger.config

import io.elixxir.xxclient.bindings.Bindings

interface ClientBridge {
    val bindings: Bindings
}