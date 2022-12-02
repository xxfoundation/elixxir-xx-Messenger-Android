package io.xxlabs.messenger.backup.bindings

import bindings.UserDiscovery
import io.xxlabs.messenger.backup.cloud.crust.BindingsCrustMediator
import io.xxlabs.messenger.backup.data.restore.RestoreLogger
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

const val BACKUP_FILE_NAME = "backup.xxm"

/**
 * Encapsulates Bindings account backup/restore functions.
 */
class BindingsBackupMediator @Inject constructor(
    preferences: PreferencesRepository,
    daoRepo: DaoRepository,
    messageReceivedListener: MessageReceivedListener,
): BackupService {

    private val backupHandler = BindingsBackupHandler(preferences)
    private val restoreHandler =
        BindingsRestoreHandler(preferences, daoRepo, messageReceivedListener, backupHandler)

    override val crustApi = BindingsCrustMediator()

    override fun initializeCrustIntegration(
        userDiscovery: UserDiscovery,
        receptionRsaPrivKey: ByteArray
    ) {
        crustApi.apply {
            udManager = userDiscovery
            receptionRsaPrivateKey = receptionRsaPrivKey
        }
    }

    override val backupFilePath: String
        get() = File(appContext().filesDir, BACKUP_FILE_NAME).path

    /**
     * Displays events related to restore progress.
     */
    override val restoreLogger: RestoreLogger by restoreHandler::restoreLogger

    override fun setListener(callback: BackupTaskCallback) {
        backupHandler.setListener(callback)
    }

    override suspend fun initializeBackup(backupPassword: String) {
        backupHandler.initializeBackup(backupPassword)
    }

    override fun isBackupRunning(): Boolean = backupHandler.isBackupRunning()

    override fun stopBackup() { backupHandler.stopBackup() }

    override fun resumeBackup() { backupHandler.resumeBackup() }

    override suspend fun restoreAccount(
        restoreParams: RestoreParams,
        callback: RestoreTaskCallback
    ) { restoreHandler.restoreAccount(restoreParams, callback) }

    override fun backupUserFacts(user: ContactWrapperBase) { backupHandler.backupUserFacts(user) }
}

data class RestoreParams(
    val ndf: String,
    val appDirectory: String,
    val sessionPassword: ByteArray,
    val backupPassword: ByteArray,
    val account: AccountArchive
)

@JvmInline
value class AccountArchive(val data: ByteArray) {
    suspend fun saveToFile(path: String): File = withContext(Dispatchers.IO) {
        val backupFile = File(path).apply {
            if (this.exists()) delete().also { Timber.d("Previous backup removed") }
        }

        runCatching {
            val outputStream = FileOutputStream(backupFile.path)
            outputStream.use { stream ->
                stream.write(data)
            }.also { Timber.d("Backup saved to $path.") }
        }

        backupFile
    }
}

interface BackupTaskCallback {
    fun onComplete(backupData: AccountArchive)
}