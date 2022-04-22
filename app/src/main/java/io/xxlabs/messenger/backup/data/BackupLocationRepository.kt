package io.xxlabs.messenger.backup.data

import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.cloud.drive.GoogleDrive
import io.xxlabs.messenger.backup.cloud.dropbox.Dropbox
import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.backup.model.AccountBackup

abstract class BackupLocationRepository(
    preferences: BackupPreferencesRepository,
    backupService: BackupService,
) : AccountBackupDataSource {

    protected val googleDrive = GoogleDrive.getInstance(backupService, preferences)
    protected val dropbox = Dropbox.getInstance(backupService, preferences)

    override val locations: List<AccountBackup> = listOf(
        googleDrive,
        dropbox
    )
}