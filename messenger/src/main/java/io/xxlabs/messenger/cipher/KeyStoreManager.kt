package io.xxlabs.messenger.cipher

interface KeyStoreManager {
    fun rsaDecryptPwd(): ByteArray
}