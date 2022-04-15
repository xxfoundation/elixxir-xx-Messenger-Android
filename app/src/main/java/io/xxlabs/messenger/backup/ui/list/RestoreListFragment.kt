package io.xxlabs.messenger.backup.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.model.RestoreOption
import io.xxlabs.messenger.ui.ConfirmDialogLauncher
import javax.inject.Inject

/**
 * Lists the backup locations to restore from.
 */
class RestoreListFragment : BackupLocationsFragment<RestoreOption>() {

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

    private val warningLauncher: ConfirmDialogLauncher by lazy {
        ConfirmDialogLauncher(requireActivity().supportFragmentManager)
    }

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

    private fun showConfirmDialog(
        title: Int,
        body: Int,
        button: Int,
        action: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        warningLauncher.showConfirmDialog(title, body, button, action, onDismiss)
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {

    }

    override fun navigateToDetail(backup: RestoreOption) {
        val directions = RestoreListFragmentDirections
            .actionRestoreListToRestoreDetail(backup)
        findNavController().navigate(directions)
    }
}