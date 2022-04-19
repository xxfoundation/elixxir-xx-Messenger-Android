package io.xxlabs.messenger.backup.ui.save

import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.model.BackupOption
import io.xxlabs.messenger.backup.model.BackupSettings
import io.xxlabs.messenger.backup.ui.list.LocationOption

interface BackupSettingsUI : BackupPasswordUI {
    val description: Spanned // for clickable info button
    val backup: BackupOption?
    val settings: LiveData<BackupSettings>
    val backupInProgress: LiveData<Boolean>
}

interface BackupSettingsController: BackupSettingsUI {
    val locations: List<SettingsOption>
    val navigateToDetail: LiveData<BackupOption?>
    val showInfoDialog: LiveData<Boolean>
    val backupError: LiveData<String?>
    fun onInfoDialogHandled()
    fun onNavigationHandled()
    fun onErrorHandled()
}