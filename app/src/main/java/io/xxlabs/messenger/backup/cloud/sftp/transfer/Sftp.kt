package io.xxlabs.messenger.backup.cloud.sftp.transfer

import android.content.Intent
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.bindings.AccountArchive
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.cloud.AuthHandler
import io.xxlabs.messenger.backup.cloud.CloudStorage
import io.xxlabs.messenger.backup.cloud.sftp.login.SshCredentials
import io.xxlabs.messenger.backup.cloud.sftp.login.ui.SshLoginActivity
import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.backup.data.restore.RestoreEnvironment
import io.xxlabs.messenger.backup.model.*
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.Exception

/**
 * Encapsulates SFTP API.
 */
class Sftp private constructor(
    private val backupService: BackupService,
    private val preferences: BackupPreferencesRepository
) : CloudStorage(backupService) {

    private val authHandler: AuthHandler by lazy {
        object : AuthHandler {
            override val signInIntent: Intent
                get() = Intent(appContext(), SshLoginActivity::class.java).apply {
                    action = SshLoginActivity.SFTP_AUTH_INTENT
                }

            override fun handleSignInResult(data: Intent?) {
                data?.getSerializableExtra(SshLoginActivity.EXTRA_SFTP_CREDENTIAL)?.run {
                    (this as? SshCredentials)?.let {
                        saveCredentials(it)
                        authResultCallback.onSuccess()
                    }
                } ?: run {
                    deleteCredentials()
                    authResultCallback.onFailure("Failed to login. Please try again.")
                }
            }

            override fun signOut() {
                deleteCredentials()
            }
        }
    }

    private var sftpClient: SftpClient? = null
        get() {
            if (field == null) {
                preferences.sftpCredential?.let {
                    field = SftpTransfer(SshCredentials.fromJson(it))
                }
            }
            return field
        }

    override val location: BackupLocation = BackupLocationData(
        R.drawable.ic_sftp,
        "SFTP",
        ::signInRequired,
        ::authBackgroundsApp,
        ::signOut,
        ::isEnabled
    ) {
        _authResultCallback = it
        authHandler
    }

    private fun saveCredentials(sftpCredentials: SshCredentials) {
        preferences.sftpCredential = sftpCredentials.toJson()
    }

    private fun deleteCredentials() {
        preferences.sftpCredential = null
    }

    private fun signInRequired(): Boolean {
        scope.launch {
            fetchData()
        }
        return preferences.sftpCredential.isNullOrEmpty()
    }

    private fun authBackgroundsApp() = false

    private fun signOut() {
        deleteCredentials()
    }

    override fun isEnabled(): Boolean = preferences.isSftpEnabled

    override fun backupNow() {
        if (isEnabled()) backup()
    }

    private fun backup() {
        scope.launch {
            updateProgress()
            File(backupService.backupFilePath).let {
                if (it.exists()) {
                    updateProgress(25)
                    upload(it)
                } else {
                    with("No backup found.") {
                        updateProgress(error = Exception(this))
                    }
                }
            }
        }
    }

    private suspend fun upload(file: File) {
        try {
            sftpClient?.upload(file)
            updateProgress(100)
            fetchData()
        } catch (e: Exception) {
            updateProgress(error = e)
        }
    }

    private val tempFile = File.createTempFile(
        "${System.currentTimeMillis()}_backup",
        ".xxm"
    )

    override fun onAuthResultSuccess() {
        scope.launch {
            fetchData()
            withContext(Dispatchers.Main) {
                _authResultCallback?.onSuccess()
            }
        }
    }

    private var cachedBackupData: AccountArchive? = null

    private suspend fun fetchData() {
        sftpClient?.download(tempFile.path)?.let {
            updateLastBackup(it.first)
            cachedBackupData = it.second
        } ?: run {
            updateLastBackup(null)
            cachedBackupData = null
        }
    }

    override suspend fun onRestore(environment: RestoreEnvironment) {
        updateProgress(25)
        cachedBackupData?.restoreUsing(environment)
    }

    companion object {
        @Volatile
        private var instance: Sftp? = null

        fun getInstance(
            backupService: BackupService,
            preferences: BackupPreferencesRepository
        ): Sftp = instance ?: Sftp(backupService, preferences).also { instance = it }
    }
}