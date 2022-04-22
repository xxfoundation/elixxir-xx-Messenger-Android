package io.xxlabs.messenger.backup.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.data.BackupSource
import io.xxlabs.messenger.ui.dialog.showConfirmDialog
import javax.inject.Inject

/**
 * Lists the backup locations to restore from.
 */
class RestoreListFragment : BackupLocationsFragment() {

    /* ViewModels */

    @Inject
    lateinit var restoreListViewModelFactory: RestoreListViewModelFactory
    override val backupViewModel: RestoreListViewModel by viewModels {
        RestoreListViewModel.provideFactory(
            restoreListViewModelFactory,
            cloudAuthentication
        )
    }

    /* UI */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMultiDeviceWarning()
    }

    private fun showMultiDeviceWarning() {
        showConfirmDialog(
            R.string.backup_restore_multidevice_warning_dialog_title,
            R.string.backup_restore_multidevice_warning_dialog_body,
            R.string.backup_restore_multidevice_warning_dialog_button,
            ::onConfirmButtonClicked,
            ::onConfirmDialogDismissed
        )
    }

    private fun onConfirmButtonClicked() {}
    private fun onConfirmDialogDismissed() {}

    override fun navigateToDetail(source: BackupSource) {
        val directions = RestoreListFragmentDirections
            .actionRestoreListToRestoreDetail(source)
        findNavController().navigate(directions)
    }
}