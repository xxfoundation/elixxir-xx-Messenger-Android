package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.backup.Backup
import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.BackupUpdateListener
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.UdId
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class InitializeBackup( private val bindings: Bindings) {

    operator fun invoke(
        e2eId: E2eId,
        udId: UdId,
        password: String,
        listener: BackupUpdateListener
    ): Result<Backup> {
        return nonNullResultOf {
            bindings.initializeBackup(
                e2eId, udId, password, listener
            )
        }
    }
}