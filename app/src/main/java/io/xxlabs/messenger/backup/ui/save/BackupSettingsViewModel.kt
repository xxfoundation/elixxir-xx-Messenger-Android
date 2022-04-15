package io.xxlabs.messenger.backup.ui.save

import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.auth.CloudAuthentication
import io.xxlabs.messenger.backup.data.BackupDataSource
import io.xxlabs.messenger.backup.model.AuthResultCallback
import io.xxlabs.messenger.backup.model.BackupLocation
import io.xxlabs.messenger.backup.model.BackupOption
import io.xxlabs.messenger.backup.model.BackupSettings
import io.xxlabs.messenger.backup.ui.list.LocationOption
import io.xxlabs.messenger.support.appContext

class BackupSettingsViewModel @AssistedInject constructor(
    private val dataSource: BackupDataSource<BackupOption>,
    @Assisted private val cloudAuthSource: CloudAuthentication,
) : ViewModel(), BackupSettingsController {

    override val description: Spanned = getSpannedDescription()
    override val backup: BackupOption? get() = dataSource.getActiveOption()
    override val settings: LiveData<BackupSettings> by dataSource::settings

    override val backupInProgress: LiveData<Boolean> = isBackupRunning()

    override val isEnabled: LiveData<Boolean> by ::_isEnabled
    private val _isEnabled = MutableLiveData(backup?.isEnabled() ?: false)

    override val locations: List<SettingsOption>
        get() = dataSource.locations.map {
                BackupSettingsOption(
                    it,
                    ::onLocationSelected,
                    ::onEnableToggled,
                    backup?.location == it,
                    it.isEnabled()
                )
            }

    override val navigateToDetail: LiveData<BackupOption?> get() = _navigateToDetail
    private val _navigateToDetail = MutableLiveData<BackupOption?>(null)

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

    private fun onEnableToggled(value: Boolean) {
        backup?.let {
            dataSource.setEnabled(value, it)
            _isEnabled.value = value
        }
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
        val highlight = appContext().getColor(R.color.brand_default)
        val description = appContext().getString(R.string.backup_setup_description)
        val highlightedText = appContext().getString(R.string.backup_setup_description_span_text)
        val startIndex = description.indexOf(highlightedText, ignoreCase = true)

        return SpannableString(description).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex,
                startIndex + highlightedText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
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
        _navigateToDetail.value = dataSource.getBackupDetails(backupLocation)
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
    val enabled: Boolean
    fun onEnableToggled(value: Boolean)
}

private data class BackupSettingsOption(
    private val location: BackupLocation,
    private val _onClick: (BackupLocation) -> Unit,
    private val _onEnableToggled: (Boolean) -> Unit,
    override val isCurrentLocation: Boolean = false,
    override val enabled: Boolean = false,
) : SettingsOption, BackupLocation by location {
    override fun onClick() = _onClick(location)
    override fun onEnableToggled(value: Boolean) = _onEnableToggled(value)
}