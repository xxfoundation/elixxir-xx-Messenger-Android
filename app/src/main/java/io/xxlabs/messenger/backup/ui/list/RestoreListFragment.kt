package io.xxlabs.messenger.backup.ui.list

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.data.BackupSource
import io.xxlabs.messenger.ui.dialog.warning.showConfirmDialog
import java.lang.Exception
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybePreventBackNavigation()
    }

    /**
     * Once choosing restore, prevent navigation back to the username flow.
     * Close the app instead.
     */
    private fun maybePreventBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        if (backupViewModel.allowBackNavigation()) {
                            requireActivity().onBackPressed()
                        } else {
                            requireActivity().finish()
                        }
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }
        )
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

    override fun navigateToDetail(source: BackupSource) {
        val directions = RestoreListFragmentDirections
            .actionRestoreListToRestoreDetail(source)
        findNavController().navigate(directions)
    }
}