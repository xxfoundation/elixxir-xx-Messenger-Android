package io.xxlabs.messenger.keystore

interface KeyStoreManager {
    suspend fun generatePassword(): Result<Unit>
    suspend fun generateKeys(): Result<Unit>
    suspend fun rsaDecryptPwd(): Result<ByteArray>
}