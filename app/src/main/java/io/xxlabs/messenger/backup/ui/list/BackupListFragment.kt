package io.xxlabs.messenger.backup.ui.list

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.backup.data.BackupSource
import javax.inject.Inject

/**
 * Lists the backup locations to save to.
 */
class BackupListFragment : BackupLocationsFragment() {

    @Inject
    lateinit var backupListViewModelFactory: BackupListViewModelFactory
    override val backupViewModel: BackupListViewModel by viewModels {
        BackupListViewModel.provideFactory(
            backupListViewModelFactory,
            cloudAuthentication
        )
    }

    override fun navigateToDetail(source: BackupSource) {
        val directions = BackupListFragmentDirections
            .actionBackupListToBackupDetail(source)
        findNavController().navigate(directions)
    }
}