package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.backup.Backup
import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.BackupUpdateListener
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.UdId
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class InitializeBackup(
    private val bindings: Bindings,
    private val e2eId: () -> E2eId,
    private val udId: () -> UdId,
    private val password: () -> String,
    private val listener: () -> BackupUpdateListener
) {

    operator fun invoke(): Result<Backup> {
        return nonNullResultOf {
            bindings.initializeBackup(
                e2eId(), udId(), password(), listener()
            )
        }
    }
}