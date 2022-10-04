package io.elixxir.xxmessengerclient.utils

import io.elixxir.xxclient.callbacks.AuthEventListener
import java.util.UUID

class AuthCallbacksRegistry {
    val authCallbacks: MutableMap<UUID, AuthEventListener> = mutableMapOf()
}