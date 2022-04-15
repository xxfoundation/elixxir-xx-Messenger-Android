package io.xxlabs.messenger.backup.data

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.model.BackupLocation
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.backup.model.BackupSettings

interface BackupDataSource<T : AccountBackup> {
    val locations: List<BackupLocation>
    val settings: LiveData<BackupSettings>

    fun getActiveOption(): T?
    fun setLocation(location: BackupLocation): T
    fun setEnabled(enabled: Boolean, backup: AccountBackup)
    fun getBackupDetails(location: BackupLocation): T
    fun setNetwork(network: BackupSettings.Network)
    fun setFrequency(frequency: BackupSettings.Frequency)
}

