package io.elixxir.xxmessengerclient.utils

import io.elixxir.xxclient.callbacks.MessageListener
import java.util.UUID

class ListenersRegistry {
    val listeners: MutableMap<UUID, MessageListener> = mutableMapOf()
}