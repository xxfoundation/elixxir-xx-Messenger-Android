package io.xxlabs.messenger.backup.data

import io.xxlabs.messenger.backup.model.AccountBackup

interface AccountBackupDataSource {
    val locations: List<AccountBackup>
}