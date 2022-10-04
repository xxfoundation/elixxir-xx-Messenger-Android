package io.elixxir.xxmessengerclient.utils

import io.elixxir.xxclient.models.BackupUpdateListener
import java.util.*

class BackupCallbacksRegistry {
    val callbacks: MutableMap<UUID, BackupUpdateListener> = mutableMapOf()
}