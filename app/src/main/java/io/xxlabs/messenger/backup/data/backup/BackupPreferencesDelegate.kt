package io.xxlabs.messenger.backup.data.backup

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.backup.cloud.crust.Crust
import io.xxlabs.messenger.backup.cloud.drive.GoogleDrive
import io.xxlabs.messenger.backup.cloud.dropbox.Dropbox
import io.xxlabs.messenger.backup.cloud.sftp.transfer.Sftp
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BackupPreferencesDelegate(
    private val preferences: BackupPreferencesRepository,
    private val backupManager: BackupManager,
) : BackupPreferences {

    private val scope =  CoroutineScope(
        CoroutineName("BackupPreferencesDelegate")
                + Job()
                + Dispatchers.Default
    )

    private val network: ConnectivityManager by lazy {
        appContext().getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
    }

    private val backupSettings: BackupSettings
        get() {
            return object : BackupSettings {
                override val frequency: BackupSettings.Frequency
                    get() = if (preferences.autoBackup) BackupSettings.Frequency.AUTOMATIC
                    else BackupSettings.Frequency.MANUAL
                override val network: BackupSettings.Network
                    get() = if (preferences.wiFiOnlyBackup) BackupSettings.Network.WIFI_ONLY
                    else BackupSettings.Network.ANY
            }
        }

    override val settings: LiveData<BackupSettings> by ::_settings
    private val _settings = MutableLiveData(backupSettings)

    override val settingsFlow: StateFlow<BackupSettings> by ::_settingsFlow
    private val _settingsFlow = MutableStateFlow(backupSettings)

    private fun tryBackup() {
        if (preferences.autoBackup && networkPreferencesMet()) {
            backupManager.getActiveBackupOption()?.backupNow()
        }
    }

    override fun setEnabled(enabled: Boolean, backup: AccountBackup) {
        preferences.isBackupEnabled = enabled
        when (backup) {
            is GoogleDrive -> googleDriveEnabled(enabled)
            is Dropbox -> dropboxEnabled(enabled)
            is Sftp -> sftpEnabled(enabled)
            is Crust -> crustEnabled(enabled)
        }
        reflectChanges()
        if (enabled) tryBackup()
    }

    private fun googleDriveEnabled(enabled: Boolean) {
        preferences.isGoogleDriveEnabled = enabled
        if (enabled) {
            preferences.isDropboxEnabled = false
            preferences.isSftpEnabled = false
            preferences.isCrustEnabled = false
        }
    }

    private fun dropboxEnabled(enabled: Boolean) {
        preferences.isDropboxEnabled = enabled
        if (enabled) {
            preferences.isGoogleDriveEnabled = false
            preferences.isSftpEnabled = false
            preferences.isCrustEnabled = false
        }
    }

    private fun sftpEnabled(enabled: Boolean) {
        preferences.isSftpEnabled = enabled
        if (enabled) {
            preferences.isGoogleDriveEnabled = false
            preferences.isDropboxEnabled = false
            preferences.isCrustEnabled = false
        }
    }

    private fun crustEnabled(enabled: Boolean) {
        preferences.isCrustEnabled = enabled
        if (enabled) {
            preferences.isGoogleDriveEnabled = false
            preferences.isDropboxEnabled = false
            preferences.isSftpEnabled = false
        }
    }

    override fun setNetwork(network: BackupSettings.Network) {
        preferences.wiFiOnlyBackup = when (network) {
            BackupSettings.Network.WIFI_ONLY -> true
            else -> false
        }
        tryBackup()
        reflectChanges()
    }

    override fun setFrequency(frequency: BackupSettings.Frequency) {
        preferences.autoBackup = when (frequency) {
            BackupSettings.Frequency.AUTOMATIC -> true
            else -> false
        }
        tryBackup()
        reflectChanges()
    }

    private fun reflectChanges() {
        _settings.postValue(backupSettings)
        scope.launch {
            _settingsFlow.emit(backupSettings)
        }
    }

    private fun networkPreferencesMet(): Boolean {
        return when (preferences.wiFiOnlyBackup) {
            true -> network.isWiFi()
            false -> network.isOnline()
        }
    }

    private fun ConnectivityManager.isOnline(): Boolean {
        return activeNetworkInfo?.isConnected == true
    }

    private fun ConnectivityManager.isWiFi(): Boolean {
        return isOnline()
                && getNetworkInfo(activeNetwork)?.type == ConnectivityManager.TYPE_WIFI
    }
}