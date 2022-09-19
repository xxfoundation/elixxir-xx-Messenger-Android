package io.xxlabs.messenger.keystore

interface KeyStoreManager {
    suspend fun generatePassword(): Result<Unit>
    suspend fun decryptPassword(): Result<ByteArray>
}