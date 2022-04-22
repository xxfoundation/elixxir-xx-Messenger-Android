package io.xxlabs.messenger.backup.ui.backup

import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.data.backup.BackupSettings
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.ui.dialog.textinput.TextInputDialogUI
import io.xxlabs.messenger.ui.dialog.radiobutton.RadioButtonDialogUI

interface BackupPasswordUI {
    val isBackupReady: LiveData<Boolean>
    val isEnabled: LiveData<Boolean>
    val showSetPasswordPrompt: LiveData<TextInputDialogUI?>
    fun onEnableToggled(enabled: Boolean)
    fun onPasswordPromptHandled()
    fun backupNow()
}

interface BackupDetailUI : BackupPasswordUI {
    val settings: LiveData<BackupSettings>
    val backup: AccountBackup
    val description: Spanned // for clickable info button
    val backupDisclaimer: String
    val backupFrequencyLabel: String
    val backupInProgress: LiveData<Boolean>
    val lastBackupDate: LiveData<Long?>
    fun onCancelClicked()
    fun onFrequencyClicked()
    fun onNetworkClicked()
}

interface BackupDetailController : BackupDetailUI {
    val showInfoDialog: LiveData<Boolean>
    val showFrequencyOptions: LiveData<RadioButtonDialogUI?>
    val showNetworkOptions: LiveData<RadioButtonDialogUI?>
    val backupError: LiveData<Throwable?>
    val backupSuccess: LiveData<Boolean>
    fun onInfoDialogHandled()
    fun onFrequencyOptionsHandled()
    fun onNetworkOptionsHandled()
}