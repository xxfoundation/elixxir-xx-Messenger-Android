package io.xxlabs.messenger.backup.bindings

import bindings.Backup
import bindings.Bindings
import bindings.Client
import bindings.UpdateBackupFunc
import io.xxlabs.messenger.backup.data.restore.ExtrasJson
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File


/**
 * Backs up an account by calling methods exposed in Bindings.
 */
class BindingsBackupHandler(private val preferences: PreferencesRepository) {

    private val scope = CoroutineScope(
        CoroutineName("BackupHandler") +
                Job() +
                Dispatchers.IO
    )

    private var backup: Backup? = null
    private val client: Client
        get() = ClientRepository.clientWrapper.client
    private val userWrapper: ContactWrapperBase
        get() = ClientRepository.userWrapper

    private val backupTaskCallback: BackupTaskCallback
        get() = object : BackupTaskCallback {
            override fun onComplete(backupData: AccountArchive) {
                Timber.d("Created backup: ${backupData.data.size} bytes")
                scope.launch {
                    backupData.saveToFile(backupFilePath)
                    listener?.onComplete(backupData)
                }
            }
        }


    /** Notified when Bindings have completed a backup. */
    private var listener: BackupTaskCallback? = null

    val backupFilePath: String
        get() = File(appContext().filesDir, BACKUP_FILE_NAME).path

    fun setListener(callback: BackupTaskCallback) {
        listener = callback
    }

    fun initializeBackup(backupPassword: String) {
        backup = Bindings.initializeBackup(
            backupPassword,
            BackupCallback(backupTaskCallback),
            client
        )
        backupUserFacts(userWrapper)
    }

    fun initializeBackupDuringRestore(client: Client, backupPassword: String? = "") {
        backup = Bindings.initializeBackup(
            backupPassword,
            BackupCallback(backupTaskCallback),
            client
        )
    }

    fun backupUserFacts(user: ContactWrapperBase) {
        if (!preferences.isUserProfileBackedUp) {
            getEnabledBackup()?.run {
                addJson(
                    ExtrasJson(
                        user.getUsernameFact(false),
                        user.getEmailFact(false),
                        user.getPhoneFact(false)
                    ).toString()
                )
                preferences.isUserProfileBackedUp = true
            }
        }
    }

    fun isBackupRunning(): Boolean = backup?.isBackupRunning ?: false

    fun stopBackup() {
//        getEnabledBackup()?.stopBackup()
    }

    fun resumeBackup() {
        if (isBackupRunning()) return
        backup = getEnabledBackup()
    }

    private fun getEnabledBackup(): Backup? {
        return backup ?: try {
            Bindings.resumeBackup(BackupCallback(backupTaskCallback), client)
        } catch (e: Exception) {
            Timber.d("resumeBackup exception: ${e.message}")
            null
        }
    }
}

private class BackupCallback(
    private val callback: BackupTaskCallback
) : BackupTaskCallback by callback, UpdateBackupFunc {

    override fun updateBackup(data: ByteArray?) {
        data?.let {
            callback.onComplete(AccountArchive(it))
        }
    }
}
