package io.xxlabs.messenger.backup.bindings

import io.xxlabs.messenger.backup.data.RestoreLogger
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase

interface BackupService {
    val backupFilePath: String
    /**
     * Assists in debugging restore-related issues.
     */
    val restoreLogger: RestoreLogger

    /**
     * Set a [BackupTaskCallback] to be notified whenever a new backup file is created.
     */
    fun setListener(callback: BackupTaskCallback)

    /**
     * Called when the backup service is enabled, to set a new [backupPassword], or to force
     * a backup to run.
     */
    fun initializeBackup(backupPassword: String = "")

    /**
     * Returns true if the backup service is already enabled.
     */
    fun isBackupRunning(): Boolean

    /**
     * Disables the backup service.
     */
    fun stopBackup()

    /**
     * Called when app returns to foreground, before network follower is initialized.
     */
    fun resumeBackup()

    /**
     * Begin the restore process using the [RestoreParams] to create a new session, and a
     * [RestoreTaskCallback] to be notified of completion or failure.
     */
    suspend fun restoreAccount(restoreParams: RestoreParams, callback: RestoreTaskCallback)

    /**
     * Called when user's email, phone number, etc. are modified so they may
     * be saved to the backup.
     */
    fun backupUserFacts(user: ContactWrapperBase)
}