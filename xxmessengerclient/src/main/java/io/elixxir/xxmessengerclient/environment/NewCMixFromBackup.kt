package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.models.BackupReport
import io.elixxir.xxclient.utils.Data
import io.elixxir.xxclient.utils.Password
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class NewCMixFromBackup(private val bindings: Bindings) {

    operator fun invoke(
        ndfJson: String,
        storageDir: String,
        backupPassword: String,
        sessionPassword: Password,
        backupFileContents: Data,
    ): Result<BackupReport> {
        return nonNullResultOf {
            bindings.newCmixFromBackup(
                ndfJson,
                storageDir,
                backupPassword,
                sessionPassword,
                backupFileContents
            )
        }
    }
}