package io.xxlabs.messenger.backup.data

import io.xxlabs.messenger.backup.model.AccountBackup

interface AccountBackupDataSource {
    val locations: List<AccountBackup>
    fun getBackupFrom(source: BackupSource): AccountBackup
    fun getSourceFor(backup: AccountBackup): BackupSource?
}