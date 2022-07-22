package io.xxlabs.messenger.backup.data.restore

import io.xxlabs.messenger.backup.data.AccountBackupDataSource
import io.xxlabs.messenger.backup.model.AccountBackup

interface RestoreManager : AccountBackupDataSource {
    val restoreAttempted: Boolean
    fun getRestoreLog(backup: AccountBackup): RestoreLog?
    suspend fun restore(backup: AccountBackup, environment: RestoreEnvironment)
    fun cancelRestore(backup: AccountBackup)
}