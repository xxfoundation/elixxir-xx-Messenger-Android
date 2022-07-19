package io.xxlabs.messenger.backup.ui.list

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.cloud.CloudAuthentication
import io.xxlabs.messenger.backup.data.restore.RestoreManager
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.backup.model.BackupLocation
import io.xxlabs.messenger.support.appContext

class RestoreListViewModel @AssistedInject constructor(
    private val restoreManager: RestoreManager,
    @Assisted cloudAuthSource: CloudAuthentication,
) : BackupLocationsViewModel(restoreManager, cloudAuthSource) {

    override val backupLocationsTitle: Spanned = getSpannableTitle()
    override val backupLocationsDescription: Spanned = getSpannableDescription()

    private fun getSpannableTitle(): Spanned {
        val title = appContext().getString(R.string.backup_restore_title)
        return SpannableString(title)
    }

    private fun getSpannableDescription(): Spanned {
        val description = appContext().getString(R.string.backup_restore_description)
        return SpannableStringBuilder(description)
            .append(appContext().getString(R.string.backup_restore_description_hint))
    }

    override fun onLocationSelected(backupLocation: BackupLocation) {
        with(backupLocation) {
            signOut()
            super.onLocationSelected(this)
        }
    }

    override fun onAuthSuccess(backupLocation: BackupLocation) {
        if (getAccountBackup(backupLocation)?.hasBackup() == true) {
            navigateToDetail(backupLocation)
        } else {
            setError(appContext().getString(R.string.backup_restore_error_no_backup_found))
        }
    }

    private fun AccountBackup.hasBackup(): Boolean =
        lastBackup.value?.run {
            sizeBytes > 0
        } ?: false

    fun allowBackNavigation(): Boolean = !restoreManager.restoreAttempted

    companion object {
        fun provideFactory(
            assistedFactory: RestoreListViewModelFactory,
            cloudAuthSource: CloudAuthentication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(cloudAuthSource) as T
            }
        }
    }
}

@AssistedFactory
interface RestoreListViewModelFactory {
    fun create(cloudAuthSource: CloudAuthentication): RestoreListViewModel
}
