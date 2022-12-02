package io.xxlabs.messenger.backup.cloud.crust

import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.bindings.AccountArchive
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.cloud.CloudStorage
import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.backup.data.restore.RestoreEnvironment
import io.xxlabs.messenger.backup.model.BackupLocation
import io.xxlabs.messenger.backup.model.BackupSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Encapsulates Crust storage API.
 */
class Crust private constructor(
    private val backupService: BackupService,
    private val preferences: BackupPreferencesRepository,
    private val crustApi: CrustDataSource
) : CloudStorage(backupService) {

    private var cachedBackupData: AccountArchive? = null

    override val location: BackupLocation = BackupLocationData(
        R.drawable.ic_sftp,
        "Crust",
        ::signInRequired,
        ::authBackgroundsApp,
        ::signOut,
        ::isEnabled
    ) {
        null
    }

    private fun signInRequired(): Boolean {
        return if (preferences.name.isBlank()) {
            true
        } else {
            onAuthResultSuccess()
            false
        }
    }

    private fun authBackgroundsApp() = false

    private fun signOut() {}

    override fun onAuthResultSuccess() {
        scope.launch {
            fetchData()
            withContext(Dispatchers.Main) {
                _authResultCallback?.onSuccess()
            }
        }
    }

    private suspend fun fetchData() {
        cachedBackupData = crustApi.recoverBackup(preferences.name).getOrNull()?.let {
            AccountArchive(it)
        }?.also {
            updateLastBackup(CrustBackupData.from(it))
        }
    }

    override suspend fun onRestore(environment: RestoreEnvironment) {
        updateProgress(25)
        cachedBackupData?.restoreUsing(environment)
    }

    override fun isEnabled(): Boolean {
        return preferences.isCrustEnabled
    }

    override fun backupNow() {
        if (isEnabled()) backup()
    }

    private fun backup() {
        scope.launch {
            updateProgress()
            crustApi.uploadBackup(backupService.backupFilePath).run {
                when {
                    isSuccess -> updateProgress(100)
                    isFailure -> updateProgress(error = exceptionOrNull())
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: Crust? = null

        fun getInstance(
            backupService: BackupService,
            preferences: BackupPreferencesRepository,
            crustApi: CrustDataSource
        ): Crust = instance ?: Crust(backupService, preferences, crustApi)
    }
}

private class CrustBackupData(
    override val sizeBytes: Long,
    override val date: Long = System.currentTimeMillis()
) : BackupSnapshot {

    companion object Factory {
        fun from(backupData: AccountArchive) = CrustBackupData(backupData.data.size.toLong())
    }
}