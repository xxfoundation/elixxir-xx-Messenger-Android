package io.xxlabs.messenger.backup.model

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.cloud.AuthHandler
import io.xxlabs.messenger.backup.cloud.AuthResultCallback
import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable

/**
 * An account backup or restore option.
 */
interface AccountBackup : Serializable {
    val location: BackupLocation
    @Deprecated("Use lastBackupFlow")
    val lastBackup: LiveData<BackupSnapshot?>
    val lastBackupFlow: StateFlow<BackupSnapshot?>
    @Deprecated("Use progressFlow")
    val progress: LiveData<BackupProgress?>
    val progressFlow: StateFlow<BackupProgress?>
}

/**
 * Describes the storage location or provider for an [AccountBackup].
 */
interface BackupLocation : Serializable {
    val icon: Int
    val name: String
    fun isEnabled(): Boolean
    fun signInRequired(): Boolean
    fun authBackgroundsApp(): Boolean
    fun createAuthHandler(callback: AuthResultCallback): AuthHandler?
    fun signOut()
}

/**
 * Metadata about a backup in a [BackupLocation].
 */
interface BackupSnapshot : Serializable {
    val date: Long
    val sizeBytes: Long
}

/**
 * Exposes details about a transfer in progress.
 */
interface BackupProgress {
    val bytesTransferred: Long
    val bytesTotal: Long
    val error: Throwable?
    val indeterminate: Boolean
    fun cancel()
}