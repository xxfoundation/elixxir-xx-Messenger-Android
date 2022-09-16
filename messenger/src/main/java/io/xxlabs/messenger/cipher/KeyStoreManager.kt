package io.xxlabs.messenger.cipher

interface KeyStoreManager {
    suspend fun generatePassword(): Result<Unit>
    fun rsaDecryptPwd(): Result<ByteArray>
}