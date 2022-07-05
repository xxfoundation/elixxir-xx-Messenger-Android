package io.xxlabs.messenger.backup.cloud.sftp

import android.content.Intent
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.cloud.AuthHandler
import io.xxlabs.messenger.backup.cloud.CloudStorage
import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.backup.data.restore.RestoreEnvironment
import io.xxlabs.messenger.backup.model.*
import io.xxlabs.messenger.support.appContext

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
                get() = Intent(appContext(), SftpAuthActivity::class.java).apply {
                    action = SftpAuthActivity.SFTP_AUTH_INTENT
                }

            override fun handleSignInResult(data: Intent?) {
                data?.getSerializableExtra(SftpAuthActivity.EXTRA_SFTP_CREDENTIAL)?.run {
                    (this as? SftpCredentials)?.let {
                        saveCredentials(it)
                        authResultCallback.onSuccess()
                    }
                } ?: run {
                    authResultCallback.onFailure("Failed to login. Please try again.")
                }
            }

            override fun signOut() {
                deleteCredentials()
            }
        }
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

    private fun saveCredentials(sftpCredentials: SftpCredentials) {

    }

    private fun deleteCredentials() {

    }

    private fun signInRequired(): Boolean = true

    private fun authBackgroundsApp() = false

    private fun signOut() {
        deleteCredentials()
    }

    override fun isEnabled(): Boolean {
        TODO("Check preferences if sftp is enabled")
    }

    override fun backupNow() {
        TODO("Start upload, if backup is enabled")
    }

    override fun onAuthResultSuccess() {
        TODO("Get latest backup data, if it exists")
    }

    override suspend fun onRestore(environment: RestoreEnvironment) {
        TODO("Download backup as an AccountArchive object")
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