package io.xxlabs.messenger.backup.cloud.crust

import bindings.Bindings
import bindings.UserDiscovery

interface CrustDataSource {
    suspend fun uploadBackup(path: String): ByteArray
    suspend fun recoverBackup(username: String): Result<ByteArray>
}

class BindingsCrustMediator(
    var udManager: UserDiscovery? = null,
    var receptionRsaPrivateKey: ByteArray = byteArrayOf()
) : CrustDataSource {


    override suspend fun uploadBackup(path: String): ByteArray {
        return udManager?.let {
            Bindings.uploadBackup(path, udManager, receptionRsaPrivateKey)
        } ?: byteArrayOf()
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