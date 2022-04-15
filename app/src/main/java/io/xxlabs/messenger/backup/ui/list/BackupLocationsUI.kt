package io.xxlabs.messenger.backup.ui.list

import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.support.dialog.confirm.ConfirmDialogUI
import io.xxlabs.messenger.ui.main.chats.TwoButtonInfoDialog
import io.xxlabs.messenger.ui.main.chats.TwoButtonInfoDialogUI

interface BackupLocationsUI {
    val backupLocationsTitle: Spanned
    val backupLocationsDescription: Spanned
    val isLoading: LiveData<Boolean>
}

interface BackupLocationsController<T: AccountBackup>: BackupLocationsUI {
    val locations: List<LocationOption>
    val navigateToDetail: LiveData<T?>
    val authLaunchConsentDialog: LiveData<TwoButtonInfoDialogUI?>
    val backupError: LiveData<String?>
    fun onNavigationHandled()
    fun onErrorHandled()
    fun onLaunchConsentHandled()
}