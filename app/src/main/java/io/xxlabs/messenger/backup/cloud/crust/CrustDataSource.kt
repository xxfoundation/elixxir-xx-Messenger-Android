package io.xxlabs.messenger.backup.cloud.crust

import bindings.Bindings
import bindings.UserDiscovery
import timber.log.Timber

interface CrustDataSource {
    suspend fun uploadBackup(path: String): Result<ByteArray>
    suspend fun recoverBackup(username: String): Result<ByteArray>
}

class BindingsCrustMediator(
    var udManager: UserDiscovery? = null,
    var receptionRsaPrivateKey: ByteArray = byteArrayOf(),
    val username: String? = null
) : CrustDataSource {

    override suspend fun uploadBackup(path: String): Result<ByteArray> {
        return try {
            updateUsernameCache()
            udManager?.let {
                val uploadSuccessReport = Bindings.uploadBackup(path, udManager, receptionRsaPrivateKey)
                Result.success(uploadSuccessReport)
            } ?: Result.failure(Exception("Failed to run backup. UserDiscovery not initialized."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun updateUsernameCache() {
        udManager?.run {
            try {
                if (username.isNotBlank()) {
                    storeUsername(username)
                    Timber.d("Successfully cached username '$username' in UserDiscovery.")
                }
            } catch (e: Exception) {
                Timber.d("Failed to cache username '$username' in UserDiscovery")
            }
        }
    }

    override suspend fun recoverBackup(username: String): Result<ByteArray> {
        return try {
            val backupData = Bindings.recoverBackup(username)
            Result.success(backupData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}