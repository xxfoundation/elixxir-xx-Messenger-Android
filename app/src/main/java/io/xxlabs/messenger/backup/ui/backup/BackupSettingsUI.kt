package io.xxlabs.messenger.backup.ui.backup

import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.data.backup.BackupSettings
import io.xxlabs.messenger.backup.model.AccountBackup

interface BackupSettingsUI : BackupPasswordUI {
    val description: Spanned // for clickable info button
    val backup: AccountBackup?
    val settings: LiveData<BackupSettings>
    val backupInProgress: LiveData<Boolean>
}

interface BackupSettingsController: BackupSettingsUI {
    val locations: List<SettingsOption>
    val navigateToDetail: LiveData<AccountBackup?>
    val showInfoDialog: LiveData<Boolean>
    val backupError: LiveData<String?>
    fun onInfoDialogHandled()
    fun onNavigationHandled()
    fun onErrorHandled()
}