package io.xxlabs.messenger.biometrics

interface CipherPromptCallback {
    fun onSuccess(key: ByteArray, iv: ByteArray)
    fun onError(error: String?)
}