package io.xxlabs.messenger.backup.data.restore

import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.data.BackupLocationRepository
import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.backup.model.AccountBackup
import javax.inject.Inject

class RestoreMediator @Inject constructor(
    preferences: BackupPreferencesRepository,
    backupService: BackupService
) : BackupLocationRepository(preferences, backupService),
    RestoreManager {

    override var restoreAttempted = false
        private set

    override fun getRestoreLog(backup: AccountBackup): RestoreLog? =
        (backup as? RestoreOption)?.restoreLog

    override suspend fun restore(backup: AccountBackup, environment: RestoreEnvironment) {
        restoreAttempted = true
        (backup as? RestoreOption)?.restore(environment)
    }

    override fun cancelRestore(backup: AccountBackup) {
        (backup as? RestoreOption)?.cancelRestore()
    }
}