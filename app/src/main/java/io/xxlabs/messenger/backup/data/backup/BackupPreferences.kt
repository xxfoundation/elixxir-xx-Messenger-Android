package io.xxlabs.messenger.backup.data.backup

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.model.AccountBackup
import kotlinx.coroutines.flow.StateFlow

interface BackupPreferences {
    val settings: LiveData<BackupSettings>
    val settingsFlow: StateFlow<BackupSettings>
    fun setEnabled(enabled: Boolean, backup: AccountBackup)
    fun setNetwork(network: BackupSettings.Network)
    fun setFrequency(frequency: BackupSettings.Frequency)
}