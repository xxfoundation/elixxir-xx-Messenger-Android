package io.xxlabs.messenger.backup.data.backup

import io.xxlabs.messenger.backup.model.AccountBackup

/**
 * Saves an account to an [AccountBackup].
 */
interface BackupOption : AccountBackup {
    fun isEnabled(): Boolean
    fun backupNow()
}