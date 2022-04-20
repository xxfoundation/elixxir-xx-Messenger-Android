package io.xxlabs.messenger.backup.ui.save

import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.model.BackupOption
import io.xxlabs.messenger.backup.model.BackupSettings

interface BackupPasswordUI {
    val isBackupReady: LiveData<Boolean>
    val isEnabled: LiveData<Boolean>
    val showSetPasswordPrompt: LiveData<EditTextTwoButtonDialogUI?>
    fun onEnableToggled(enabled: Boolean)
    fun onPasswordPromptHandled()
}

interface BackupDetailUI : BackupPasswordUI {
    val settings: LiveData<BackupSettings>
    val backup: BackupOption
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