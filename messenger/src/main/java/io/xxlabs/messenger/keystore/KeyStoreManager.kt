package io.xxlabs.messenger.keystore

interface KeyStoreManager {
    suspend fun generatePassword(): Result<Unit>
    fun rsaDecryptPwd(): Result<ByteArray>
}