package io.xxlabs.messenger.backup.ui.backup

import android.text.SpannableString
import android.text.Spanned
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.cloud.AuthResultCallback
import io.xxlabs.messenger.backup.cloud.CloudAuthentication
import io.xxlabs.messenger.backup.data.BackupSource
import io.xxlabs.messenger.backup.data.backup.BackupManager
import io.xxlabs.messenger.backup.data.backup.BackupOption
import io.xxlabs.messenger.backup.data.backup.BackupSettings
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.backup.model.BackupLocation
import io.xxlabs.messenger.backup.ui.list.LocationOption
import io.xxlabs.messenger.support.appContext

class BackupSettingsViewModel @AssistedInject constructor(
    backupManager: BackupManager,
    @Assisted private val cloudAuthSource: CloudAuthentication,
) : BackupViewModel(backupManager), BackupSettingsController {

    override val description: Spanned = getSpannedDescription()
    override val backup: AccountBackup? get() = backupManager.getActiveBackupOption()
    override val settings: LiveData<BackupSettings> = backupManager.settings.asLiveData()

    override val backupInProgress: LiveData<Boolean> = isBackupRunning()

    override val locations: List<SettingsOption>
        get() = backupManager.locations.map {
            backupLocationsMap[it.location] = it
            BackupSettingsOption(
                it.location,
                ::onLocationSelected,
                ::onEnableToggled,
                backup?.location == it.location,
                isEnabled
            )
        }

    private val backupLocationsMap: MutableMap<BackupLocation, AccountBackup> = mutableMapOf()

    override val navigateToDetail: LiveData<BackupSource?> get() = _navigateToDetail
    private val _navigateToDetail = MutableLiveData<BackupSource?>(null)

    private val _showInfoDialog = MutableLiveData(false)
    override val showInfoDialog: LiveData<Boolean> =
        Transformations.distinctUntilChanged(_showInfoDialog)

    override val backupError: LiveData<String?> get() = _backupError
    private val _backupError = MutableLiveData<String?>(null)

    private fun isBackupRunning(): LiveData<Boolean> {
        return backup?.let { backupOption ->
            Transformations.map(backupOption.progress) { progress ->
                progress?.run {
                    null == error && bytesTransferred != bytesTotal
                } ?: false
            }
        } ?: MutableLiveData(false)
    }

    override fun getBackupOption(): BackupOption? {
        return backup as? BackupOption
    }

    override fun onInfoDialogHandled() {
        _showInfoDialog.value = false
    }

    override fun onNavigationHandled() {
        _navigateToDetail.value = null
    }

    override fun onErrorHandled() {
        _backupError.value = null
    }

    private fun getSpannedDescription(): Spanned {
        val description = appContext().getString(R.string.backup_setup_description)
        return SpannableString(description)
    }

    private fun onLocationSelected(backupLocation: BackupLocation) {
        with (backupLocation) {
            if (signInRequired()) signIn(this)
            else navigateToDetail(this)
        }
    }

    private fun signIn(backupLocation: BackupLocation) {
        val authHandler = backupLocation.createAuthHandler(
            object : AuthResultCallback {
                override fun onFailure(errorMsg: String) { _backupError.value = errorMsg }
                override fun onSuccess() = navigateToDetail(backupLocation)
            }
        )
        authHandler?.let {
            cloudAuthSource.signIn(it)
        } ?: run { navigateToDetail(backupLocation) }
    }

    private fun navigateToDetail(backupLocation: BackupLocation) {
        backupLocationsMap[backupLocation]?.let { backup ->
            backupManager.getSourceFor(backup)?.let { source ->
                _navigateToDetail.value = source
            }
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: BackupSettingsViewModelFactory,
            cloudAuthSource: CloudAuthentication
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(cloudAuthSource) as T
            }
        }
    }
}

@AssistedFactory
interface BackupSettingsViewModelFactory {
    fun create(cloudAuthSource: CloudAuthentication): BackupSettingsViewModel
}

interface SettingsOption : LocationOption {
    val isCurrentLocation: Boolean
    val enabled: LiveData<Boolean>
    fun onEnableToggled(value: Boolean)
}

private data class BackupSettingsOption(
    private val location: BackupLocation,
    private val _onClick: (BackupLocation) -> Unit,
    private val _onEnableToggled: (Boolean) -> Unit,
    override val isCurrentLocation: Boolean,
    override val enabled: LiveData<Boolean>,
) : SettingsOption, BackupLocation by location {
    override fun onClick() = _onClick(location)
    override fun onEnableToggled(value: Boolean) = _onEnableToggled(value)
}