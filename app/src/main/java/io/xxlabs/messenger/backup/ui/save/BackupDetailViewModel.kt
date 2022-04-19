package io.xxlabs.messenger.backup.ui.save

import android.text.SpannableString
import android.text.Spanned
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.data.BackupDataSource
import io.xxlabs.messenger.backup.model.BackupOption
import io.xxlabs.messenger.backup.model.BackupSettings
import io.xxlabs.messenger.backup.model.BackupSettings.*
import io.xxlabs.messenger.support.appContext

class BackupDetailViewModel @AssistedInject constructor(
    dataSource: BackupDataSource<BackupOption>,
    @Assisted override val backup: BackupOption
) : BackupViewModel(dataSource), BackupDetailController {

    override val settings: LiveData<BackupSettings> by dataSource::settings

    override val description: Spanned = getSpannedDescription()
    override val backupInProgress: LiveData<Boolean> = Transformations.map(backup.progress) {
        it?.run {
            null == error && bytesTransferred != bytesTotal
        } ?: false
    }
    override val lastBackupDate: LiveData<Long?> = Transformations.map(backup.lastBackup) {
        it?.date
    }

    override val backupDisclaimer: String
        get() = appContext().getString(
            R.string.backup_encryption_warning,
            backup.location.name
        )
    override val backupFrequencyLabel: String
        get() = appContext().getString(
            R.string.backup_frequency_label,
            backup.location.name
        )

    private val _showInfoDialog = MutableLiveData(false)
    override val showInfoDialog: LiveData<Boolean> =
        Transformations.distinctUntilChanged(_showInfoDialog)

    override val showFrequencyOptions: LiveData<RadioButtonDialogUI?> by ::_showFrequencyOptions
    private val _showFrequencyOptions = MutableLiveData<RadioButtonDialogUI?>(null)

    override val showNetworkOptions: LiveData<RadioButtonDialogUI?> by ::_showNetworkOptions
    private val _showNetworkOptions = MutableLiveData<RadioButtonDialogUI?>(null)

    override val backupError: LiveData<Throwable?> = Transformations.map(backup.progress) {
        it?.error
    }

    override val backupSuccess: LiveData<Boolean> = Transformations.map(backup.progress) {
        it?.run {
            bytesTransferred == bytesTotal
        }
    }

    private val frequencyDialogUI: RadioButtonDialogUI by lazy {
        RadioButtonDialogUI.create(
            appContext().getString(
                R.string.backup_frequency_label,
                backup.location.name
            ),
            listOf(autoFrequencyOption, manualFrequencyOption)
        )
    }

    private val autoFrequencyOption: RadioButtonDialogOption by lazy {
        RadioButtonDialogOption.create(
            appContext().getString(R.string.backup_frequency_automatic)
        ) { onFrequencySelected(Frequency.AUTOMATIC) }
    }

    private val manualFrequencyOption: RadioButtonDialogOption by lazy {
        RadioButtonDialogOption.create(
            appContext().getString(R.string.backup_frequency_manual)
        ) { onFrequencySelected(Frequency.MANUAL) }
    }

    private val networkDialogUI: RadioButtonDialogUI by lazy {
        RadioButtonDialogUI.create(
            appContext().getString(
                R.string.backup_frequency_label,
                backup.location.name
            ),
            listOf(wifiNetworkOption, anyNetworkOption)
        )
    }

    private val wifiNetworkOption: RadioButtonDialogOption by lazy {
        RadioButtonDialogOption.create(
            appContext().getString(R.string.backup_network_wifi)
        ) { onNetworkSelected(Network.WIFI_ONLY) }
    }

    private val anyNetworkOption: RadioButtonDialogOption by lazy {
        RadioButtonDialogOption.create(
            appContext().getString(R.string.backup_network_any)
        ) { onNetworkSelected(Network.ANY) }
    }

    override fun getBackupOption(): BackupOption {
        return backup
    }

    private fun getSpannedDescription(): Spanned {
        return SpannableString("")
    }

    override fun onInfoDialogHandled() {
        _showInfoDialog.value = false
    }

    override fun onFrequencyOptionsHandled() {
        _showFrequencyOptions.value = null
    }

    override fun onNetworkOptionsHandled() {
        _showNetworkOptions.value = null
    }

    override fun onCancelClicked() {
        backup.progress.value?.cancel()
    }

    override fun onFrequencyClicked() {
        _showFrequencyOptions.value = frequencyDialogUI
    }

    override fun onNetworkClicked() {
        _showNetworkOptions.value = networkDialogUI
    }

    private fun onFrequencySelected(frequency: Frequency) {
        dataSource.setFrequency(frequency)
    }

    private fun onNetworkSelected(network: Network) {
        dataSource.setNetwork(network)
    }

    companion object {
        fun provideFactory(
            assistedFactory: BackupDetailViewModelFactory,
            backup: BackupOption
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(backup) as T
            }
        }
    }
}

@AssistedFactory
interface BackupDetailViewModelFactory {
    fun create(backup: BackupOption): BackupDetailViewModel
}