package io.elixxir.xxmessengerclient.utils

interface PasswordStorage {
    fun save(password: ByteArray)
    fun load(): ByteArray
    fun clear()
}