package io.xxlabs.messenger.backup.data

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.bindings.BackupScheduler
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.model.*
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.appContext
import java.security.InvalidParameterException
import javax.inject.Inject

/**
 * Mediates between, local backup [BackupService], backup-related [preferences] and the
 * [AccountBackup] saved to a remote [CloudStorage].
 */
abstract class AccountBackupRepository<T : AccountBackup>(
    private val preferences: PreferencesRepository,
    private val backupService: BackupService
) : BackupDataSource<T> , BackupService by backupService {

    private val settingsHandler: BackupPreferences by lazy {
        PreferencesDelegate(preferences, backupService, this)
    }
    override val settings: LiveData<BackupSettings> by settingsHandler::settings

    protected val googleDrive = GoogleDrive.getInstance(backupService, preferences)
    protected val dropbox = Dropbox.getInstance(backupService, preferences)

    override val locations: List<BackupLocation> = listOf(
        googleDrive.location,
        dropbox.location
    )

    private var currentLocation: BackupLocation? = null
        set(value) {
            preferences.backupLocation = value?.name
            field = value
        }
    var currentSelection: T? = null
        private set

    private val backupScheduler: BackupScheduler by lazy {
        BackupScheduler(preferences, this)
    }

    private val PreferencesRepository.backupOption: BackupOption?
        get() = when (backupLocation) {
            googleDrive.location.name -> googleDrive
            dropbox.location.name -> dropbox
            else -> null
        }

    init {
        backupService.setListener(backupScheduler)
    }

    fun getActiveBackupOption() = preferences.backupOption

    protected fun saveLocation(service: T): T {
        currentLocation = service.location
        return service
    }

    protected fun setCurrentSelection(service: T): T {
        currentSelection = service
        return service
    }

    override fun setEnabled(enabled: Boolean, backup: AccountBackup) {
        currentSelection?.let { if (enabled) saveLocation(it) }
        settingsHandler.setEnabled(enabled, backup)
    }

    override fun setNetwork(network: BackupSettings.Network) {
        settingsHandler.setNetwork(network)
    }

    override fun setFrequency(frequency: BackupSettings.Frequency) {
        settingsHandler.setFrequency(frequency)
    }
}

class BackupRepository @Inject constructor(
    private val preferences: PreferencesRepository,
    backupService: BackupService,
) : AccountBackupRepository<BackupOption>(preferences, backupService) {

    override fun getActiveOption(): BackupOption? = getActiveBackupOption()

    override fun setLocation(location: BackupLocation): BackupOption {
        return when (location.name) {
            appContext().getString(R.string.backup_service_google_drive) -> saveLocation(googleDrive)
            appContext().getString(R.string.backup_service_dropbox) -> saveLocation(dropbox)
            else -> throw InvalidParameterException("No service found for selected location.")
        }
    }

    override fun getBackupDetails(location: BackupLocation): BackupOption {
        return when (location.name) {
            appContext().getString(R.string.backup_service_google_drive) -> googleDrive
            appContext().getString(R.string.backup_service_dropbox) -> dropbox
            else -> throw InvalidParameterException("No service found for selected location.")
        }.apply { setCurrentSelection(this) }
    }
}

class RestoreRepository @Inject constructor(
    preferences: PreferencesRepository,
    backupService: BackupService
) : AccountBackupRepository<RestoreOption>(preferences, backupService) {

    override fun getActiveOption(): RestoreOption? = null

    override fun setLocation(location: BackupLocation): RestoreOption {
        return when (location.name) {
            appContext().getString(R.string.backup_service_google_drive) -> googleDrive
            appContext().getString(R.string.backup_service_dropbox) -> dropbox
            else -> throw InvalidParameterException("No service found for selected location.")
        }
    }

    override fun getBackupDetails(location: BackupLocation): RestoreOption {
        return when (location.name) {
            appContext().getString(R.string.backup_service_google_drive) -> googleDrive
            appContext().getString(R.string.backup_service_dropbox) -> dropbox
            else -> throw InvalidParameterException("No service found for selected location.")
        }
    }
}

interface BackupPreferences {
    val settings: LiveData<BackupSettings>
    fun setEnabled(enabled: Boolean, backup: AccountBackup)
    fun setNetwork(network: BackupSettings.Network)
    fun setFrequency(frequency: BackupSettings.Frequency)
}

/**
 * Encapsulates backup-related preferences
 */
private class PreferencesDelegate(
    private val preferences: PreferencesRepository,
    private val backupService: BackupService,
    private val backupDataSource: AccountBackupRepository<*>,
) : BackupPreferences {
    private val _settings = MutableLiveData(backupSettings)
    override val settings: LiveData<BackupSettings> by ::_settings

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

    private fun tryBackup() {
        if (preferences.autoBackup && networkPreferencesMet()) {
            backupDataSource.getActiveBackupOption()?.backupNow()
        }
    }

    override fun setEnabled(enabled: Boolean, backup: AccountBackup) {
        preferences.isBackupEnabled = enabled
        when (backup) {
            is GoogleDrive -> googleDriveEnabled(enabled)
            is Dropbox -> dropboxEnabled(enabled)
        }
        reflectChanges()
        if (enabled) tryBackup() else backupService.stopBackup()
    }

    private fun googleDriveEnabled(enabled: Boolean) {
        preferences.isGoogleDriveEnabled = enabled
        if (enabled) preferences.isDropboxEnabled = false
    }

    private fun dropboxEnabled(enabled: Boolean) {
        preferences.isDropboxEnabled = enabled
        if (enabled) preferences.isGoogleDriveEnabled = false
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
        _settings.value = backupSettings
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