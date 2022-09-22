package io.xxlabs.messenger.backup.data.restore

import io.xxlabs.messenger.backup.model.AccountBackup

/**
 * Restores an account from an [AccountBackup].
 */
interface RestoreOption : AccountBackup {
    val restoreLog: RestoreLog
    suspend fun restore(environment: RestoreEnvironment)
    fun cancelRestore()
}