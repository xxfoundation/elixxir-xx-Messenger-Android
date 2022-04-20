package io.xxlabs.messenger.backup.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.backup.model.*
import io.xxlabs.messenger.backup.model.BackupSettings.*

fun generateBackupOption(location: BackupLocation): BackupOption =
    DummyBackupOption(DummyAccountBackup(location))

fun generateDummyRestoreOption(location: BackupLocation): RestoreOption =
    DummyRestoreOption(DummyAccountBackup(location))

data class DummyAccountBackup(
    override val location: BackupLocation
) : AccountBackup {
    override val lastBackup: LiveData<BackupSnapshot?> =
        MutableLiveData<BackupSnapshot>(DummySnapshot())
    override val progress: LiveData<BackupProgress?> = MutableLiveData(DummyProgress())
    override fun isEnabled() = true
}

data class DummySnapshot(
    override val date: Long = System.currentTimeMillis() - 50_000,
    override val sizeBytes: Long = 300_000L
): BackupSnapshot

data class DummyBackupOption(
    private val accountBackup: AccountBackup
): BackupOption, AccountBackup by accountBackup {
    override fun backupNow() {}
}

data class DummyRestoreOption(
    private val accountBackup: AccountBackup
): RestoreOption, AccountBackup by accountBackup {
    override val restoreLog: RestoreLog = RestoreLogger()
    override suspend fun restore(environment: RestoreEnvironment) {}
    override fun cancelRestore() { }
}

data class DummyProgress (
    override val bytesTransferred: Long = 100L,
    override val bytesTotal: Long = 300L,
    override val error: Throwable? = null,
    override val indeterminate: Boolean = true,
): BackupProgress {
    override fun cancel() {}
}

data class DummySettings(
    override val frequency: Frequency = Frequency.MANUAL,
    override val network: Network = Network.ANY,
): BackupSettings

