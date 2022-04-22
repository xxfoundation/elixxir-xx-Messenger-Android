package io.xxlabs.messenger.backup.data.backup

import io.xxlabs.messenger.backup.data.AccountBackupDataSource
import io.xxlabs.messenger.backup.model.AccountBackup
import kotlinx.coroutines.flow.Flow

interface BackupManager : AccountBackupDataSource, BackupTaskPublisher {
    val settings: Flow<BackupSettings>
    suspend fun enableBackup(backup: AccountBackup, password: String)
    fun getActiveBackupOption(): BackupOption?
    fun disableBackup(backup: AccountBackup)
    fun setNetwork(network: BackupSettings.Network)
    fun setFrequency(frequency: BackupSettings.Frequency)
    fun backupNow(backup: AccountBackup)
}