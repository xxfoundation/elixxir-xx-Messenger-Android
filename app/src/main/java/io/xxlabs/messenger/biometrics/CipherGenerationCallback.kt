package io.xxlabs.messenger.biometrics

import javax.crypto.Cipher

interface CipherGenerationCallback {
    fun onCipherGenerationError(error: String?)
    fun onCipherGenerated(cipher: Cipher)
}