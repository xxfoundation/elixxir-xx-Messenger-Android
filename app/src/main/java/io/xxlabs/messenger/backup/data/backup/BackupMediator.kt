package io.xxlabs.messenger.backup.data.backup

import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.data.BackupLocationRepository
import io.xxlabs.messenger.backup.model.AccountBackup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BackupMediator @Inject constructor(
    private val preferences: BackupPreferencesRepository,
    private val backupService: BackupService,
    private val backupTaskPublisher: BackupTaskPublisher
) : BackupLocationRepository(preferences, backupService),
    BackupManager,
    BackupService by backupService,
    BackupTaskPublisher by backupTaskPublisher
{
    private val settingsHandler: BackupPreferencesDelegate by lazy {
        BackupPreferencesDelegate(preferences, this)
    }

    private val BackupPreferencesRepository.backupOption: BackupOption?
        get() = when (backupLocation) {
            googleDrive.location.name -> googleDrive
            dropbox.location.name -> dropbox
            sftp.location.name -> sftp
            crust.location.name -> crust
            else -> null
        }

    private val backupScheduler: AccountBackupScheduler by lazy {
        AccountBackupScheduler(preferences, this)
    }

    override val settings: Flow<BackupSettings> = settingsHandler.settingsFlow

    init {
        backupTaskPublisher.subscribe(backupScheduler)
        backupService.setListener(backupTaskPublisher)
    }

    override fun getActiveBackupOption() = preferences.backupOption

    override suspend fun enableBackup(backup: AccountBackup, password: String) {
        backupService.initializeBackup(password)
        saveLocation(backup)
        settingsHandler.setEnabled(true, backup)
    }

    private fun saveLocation(backup: AccountBackup) {
        preferences.backupLocation = backup.location.name
    }

    override fun disableBackup(backup: AccountBackup) {
        backupService.stopBackup()
        settingsHandler.setEnabled(false, backup)
    }

    override fun setNetwork(network: BackupSettings.Network) {
        settingsHandler.setNetwork(network)
    }

    override fun setFrequency(frequency: BackupSettings.Frequency) {
        settingsHandler.setFrequency(frequency)
    }

    override fun backupNow(backup: AccountBackup) {
        saveLocation(backup)
        (backup as? BackupOption)?.backupNow()
    }
}