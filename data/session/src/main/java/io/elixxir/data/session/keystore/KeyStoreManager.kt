package io.elixxir.data.session.keystore

interface KeyStoreManager {
    suspend fun generatePassword(): Result<Unit>
    suspend fun decryptPassword(): Result<ByteArray>
}