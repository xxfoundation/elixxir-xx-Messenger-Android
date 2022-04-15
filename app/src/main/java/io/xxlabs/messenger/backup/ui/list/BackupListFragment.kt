package io.xxlabs.messenger.backup.ui.list

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.backup.model.BackupOption
import javax.inject.Inject

/**
 * Lists the backup locations to save to.
 */
class BackupListFragment : BackupLocationsFragment<BackupOption>() {

    @Inject
    lateinit var backupListViewModelFactory: BackupListViewModelFactory
    override val backupViewModel: BackupListViewModel by viewModels {
        BackupListViewModel.provideFactory(
            backupListViewModelFactory,
            cloudAuthentication
        )
    }

    override fun navigateToDetail(backup: BackupOption) {
        val directions = BackupListFragmentDirections
            .actionBackupListToBackupDetail(backup)
        findNavController().navigate(directions)
    }
}