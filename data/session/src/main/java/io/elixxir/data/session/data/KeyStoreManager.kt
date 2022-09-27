package io.elixxir.data.session.data

interface KeyStoreManager {
    suspend fun generatePassword(): Result<Unit>
    suspend fun decryptPassword(): Result<ByteArray>
}