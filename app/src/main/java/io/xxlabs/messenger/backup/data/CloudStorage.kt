package io.xxlabs.messenger.backup.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.backup.bindings.AccountArchive
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.bindings.RestoreParams
import io.xxlabs.messenger.backup.bindings.RestoreTaskCallback
import io.xxlabs.messenger.backup.model.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

const val BACKUP_DIRECTORY_NAME = "backup"

/**
 * Provides a common interface for authentication, download and upload with a
 * cloud storage provider.
 */
abstract class CloudStorage(
    private val backupService: BackupService
) : AccountBackup, BackupOption, RestoreOption {

    protected val scope =  CoroutineScope(
        CoroutineName("CloudService")
                + Job()
                + Dispatchers.Default
    )

    override val lastBackup: LiveData<BackupSnapshot?> by ::_lastBackup
    protected val _lastBackup = MutableLiveData<BackupSnapshot?>()

    override val progress: LiveData<BackupProgress?> by ::_progress
    protected val _progress = MutableLiveData<BackupProgress?>(null)

    protected val authResultCallback: AuthResultCallback =
        AuthResultCallbackDelegate(::onAuthResultFailure, ::authResultSuccess)
    protected var _authResultCallback: AuthResultCallback? = null

    override val restoreLog: RestoreLog by backupService::restoreLogger
    private val restoreCallback: RestoreTaskCallback = object : RestoreTaskCallback {
        override fun onProgressUpdate(
            contactsFound: Long,
            contactsRestored: Long,
            total: Long,
            error: String?
        ) {
            _progress.postValue(ProgressData(
                contactsRestored,
                total,
                error?.run { java.lang.Exception(this) },
                false,
                { },
            ))
        }
    }

    private var taskStarted = false

    protected fun log(event: String) = backupService.restoreLogger.log(event)

    private fun authResultSuccess() {
        resetCachedProgress()
        onAuthResultSuccess()
    }

    private fun resetCachedProgress() {
        _progress.value = null
    }

    abstract fun onAuthResultSuccess()

    private fun onAuthResultFailure(errorMsg: String) = _authResultCallback?.onFailure(errorMsg)

    override fun restore(environment: RestoreEnvironment) {
        if (taskStarted) return
        taskStarted = true
        log("Restore started.")
        onRestore(environment)
    }

    abstract fun onRestore(environment: RestoreEnvironment)

    protected suspend fun AccountArchive.restoreUsing(environment: RestoreEnvironment) {
        backupService.restoreAccount(
            environment.toRestoreParams(this),
            restoreCallback
        )
    }

    protected fun updateProgress(
        progress: Long = 0L,
        total: Long = 100L,
        error: Throwable? = null,
        indeterminate: Boolean = false
    ) {
        _progress.postValue(ProgressData(
            progress,
            total,
            error,
            indeterminate,
            {}
        ))
    }

    protected data class BackupLocationData(
        override val icon: Int,
        override val name: String,
        private val _signInRequired: () -> Boolean,
        private val _authBackgroundsApp: () -> Boolean,
        private val _signOut: () -> Unit,
        private val _isEnabled: () -> Boolean,
        private val authHandler: (AuthResultCallback) -> AuthHandler?,
    ) : BackupLocation {
        override fun isEnabled(): Boolean = _isEnabled()
        override fun signInRequired(): Boolean = _signInRequired()
        override fun authBackgroundsApp(): Boolean = _authBackgroundsApp()
        override fun createAuthHandler(callback: AuthResultCallback): AuthHandler? =
            authHandler(callback)
        override fun signOut() = _signOut()
    }
}

private data class AuthResultCallbackDelegate(
    private val _onFailure: (String) -> Unit,
    private val _onSuccess: () -> Unit
) : AuthResultCallback {
    override fun onFailure(errorMsg: String) = _onFailure(errorMsg)
    override fun onSuccess() = _onSuccess()
}

private data class ProgressData(
    override val bytesTransferred: Long,
    override val bytesTotal: Long,
    override val error: Throwable?,
    override val indeterminate: Boolean,
    private val _cancel: () -> Unit
): BackupProgress {
    override fun cancel() = _cancel()
}

private fun RestoreEnvironment.toRestoreParams(account: AccountArchive) =
    RestoreParams(ndf, appDirectory, sessionPassword, backupPassword, account)