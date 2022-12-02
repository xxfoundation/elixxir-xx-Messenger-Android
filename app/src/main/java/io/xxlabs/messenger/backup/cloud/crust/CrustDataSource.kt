package io.xxlabs.messenger.backup.cloud.crust

import bindings.Bindings
import bindings.UserDiscovery

interface CrustDataSource {
    suspend fun uploadBackup(path: String): Result<ByteArray>
    suspend fun recoverBackup(username: String): Result<ByteArray>
}

class BindingsCrustMediator(
    var udManager: UserDiscovery? = null,
    var receptionRsaPrivateKey: ByteArray = byteArrayOf()
) : CrustDataSource {

    override suspend fun uploadBackup(path: String): Result<ByteArray> {
        return try {
            udManager?.let {
                val uploadSuccessReport = Bindings.uploadBackup(path, udManager, receptionRsaPrivateKey)
                Result.success(uploadSuccessReport)
            } ?: Result.failure(Exception("Failed to run backup. UserDiscovery not initialized."))
        } catch (e: Exception) {
            Result.failure(e)
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