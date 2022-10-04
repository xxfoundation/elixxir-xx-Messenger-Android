package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.models.BackupReport
import io.elixxir.xxclient.utils.Data
import io.elixxir.xxclient.utils.Password
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class NewCMixFromBackup(
    private val bindings: Bindings,
    private val ndfJson: () -> String,
    private val storageDir: () -> String,
    private val backupPassword: () -> String,
    private val sessionPassword: () -> Password,
    private val backupFileContents: () -> Data,
) {

    operator fun invoke(): Result<BackupReport> {
        return nonNullResultOf {
            bindings.newCmixFromBackup(
                ndfJson(),
                storageDir(),
                backupPassword(),
                sessionPassword(),
                backupFileContents()
            )
        }
    }
}