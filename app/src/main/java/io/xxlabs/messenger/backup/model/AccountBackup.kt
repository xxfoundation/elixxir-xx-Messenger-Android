package io.xxlabs.messenger.backup.model

import android.content.Intent
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.backup.data.RestoreLog
import io.xxlabs.messenger.support.extensions.capitalizeWords
import java.io.Serializable

/**
 * An account backup or restore option.
 */
interface AccountBackup : Serializable {
    val location: BackupLocation
    val lastBackup: LiveData<BackupSnapshot?>
    val progress: LiveData<BackupProgress?>
    fun isEnabled(): Boolean
}

/**
 * Restores an account from an [AccountBackup].
 */
interface RestoreOption : AccountBackup {
    val restoreLog: RestoreLog
    fun restore(environment: RestoreEnvironment)
}

/**
 * Necessary data to restore an account to a new session on a device.
 */
data class RestoreEnvironment(
    val ndf: String,
    val appDirectory: String,
    val sessionPassword: ByteArray,
    val backupPassword: ByteArray,
)

/**
 * Saves an account to an [AccountBackup].
 */
interface BackupOption : AccountBackup {
    fun backupNow()
}

/**
 * Preferences that determine if and when the backup runs automatically.
 */
interface BackupSettings {
    val frequency: Frequency
    val network: Network

    enum class Frequency {
        AUTOMATIC, MANUAL;

        override fun toString(): String {
            return super.toString().capitalizeWords()
        }
    }
    enum class Network {
        WIFI_ONLY {
            override fun toString() = "Wi-Fi Only"
        },
        ANY {
            override fun toString() = "Wi-Fi or Cellular"
        };
    }
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
 * Handles authentication with a [BackupLocation] that requires sign-in.
 */
interface AuthHandler {
    val signInIntent: Intent
    fun handleSignInResult(data: Intent?)
    fun signOut()
}

/**
 * Exposes the result of an [AuthHandler] authentication attempt.
 */
interface AuthResultCallback {
    fun onFailure(errorMsg: String)
    fun onSuccess()
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