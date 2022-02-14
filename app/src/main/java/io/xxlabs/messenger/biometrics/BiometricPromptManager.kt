package io.xxlabs.messenger.biometrics

import android.content.Context
import android.os.CancellationSignal
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.xxlabs.messenger.repository.PreferencesRepository
import timber.log.Timber
import javax.crypto.Cipher

class BiometricPromptManager private constructor(
    biometricBuilder: BiometricBuilder,
    preferences: PreferencesRepository
) : BiometricCipherManager(preferences) {
    private lateinit var biometricCallback: BiometricCallback
    var containerFragment: Fragment = biometricBuilder.containerFragment
    var context: Context = biometricBuilder.containerFragment.requireContext()
    private var title: String? = null
    private var subtitle: String? = null
    private var description: String? = null
    private var negativeButtonText: String? = null
    private var failedCounter: Int = 0
    private var cancellationSignal = CancellationSignal()

    fun authenticate(
        biometricCallback: BiometricCallback,
        encryptionData: String? = null
    ) {
        this.biometricCallback = biometricCallback
        if (!areBiometricsAvailable(biometricCallback)) {
            return
        }

        if (encryptionData.isNullOrEmpty()) {
            displayDecryptionPrompt(biometricCallback)
        } else {
            displayEncryptionPrompt(encryptionData, biometricCallback)
        }
    }

    private fun areBiometricsAvailable(biometricCallback: BiometricCallback): Boolean {
        if (title == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog title cannot be null")
            return false
        }

        if (subtitle == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog subtitle cannot be null")
            return false
        }

        if (description == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog description cannot be null")
            return false
        }

        if (negativeButtonText == null) {
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog negative button text cannot be null")
            return false
        }

        if (!BiometricUtils.isKeyguardActivated(context)) {
            biometricCallback.onBiometricAuthenticationNotAvailable()
            return false
        }

        if (BiometricUtils.isBiometricPromptEnabled && !BiometricUtils.isDeviceSecure(context)) {
            biometricCallback.onBiometricAuthenticationNotAvailable()
            return false
        }

        if (isCancelled) {
            Timber.v("Signal Cancelled, reseting...")
            resetCancelSignal()
        }
        return true
    }

    fun dismiss(executeOnFailed: Boolean = true) {
        Timber.v("Biometric Dismissed!")
        if (!cancellationSignal.isCanceled) {
            cancellationSignal.cancel()
        }

        if (executeOnFailed) {
            biometricCallback.onAuthenticationFailed()
        }
    }

    private fun displayEncryptionPrompt(
        dataToEncrypt: String,
        biometricCallback: BiometricCallback
    ) {
        Timber.v("Starting Biometric Prompt...")
        encryptPrompt(object : CipherGenerationCallback {
            override fun onCipherGenerationError(error: String?) {
                Timber.e("Starting Biometric Error $error")
                biometricCallback.onBiometricAuthenticationInternalError(error)
            }

            override fun onCipherGenerated(cipher: Cipher) {
                Timber.v("Biometric Prompt Encryption Successfully generated...")
                val prompt = biometricPromptInfo()
                promptBiometricEncryption(dataToEncrypt, cipher, prompt)
            }
        })
    }

    private fun displayDecryptionPrompt(
        biometricCallback: BiometricCallback
    ) {
        decryptPrompt(object : CipherGenerationCallback {
            override fun onCipherGenerationError(error: String?) {
                Timber.e("Starting Biometric Error $error")
                biometricCallback.onBiometricAuthenticationInternalError(error)
            }

            override fun onCipherGenerated(cipher: Cipher) {
                Timber.v("Biometric Prompt Decryption Successfully generated...")
                val prompt = biometricPromptInfo()
                promptBiometricDecryption(cipher, prompt)
            }
        })
    }

    private fun promptBiometricEncryption(
        dataToEncrypt: String,
        cipher: Cipher,
        prompt: BiometricPrompt.PromptInfo
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val authenticatedCipher = result.cryptoObject?.cipher

                if (authenticatedCipher != null) {
                    Timber.v("Authentication Success!!!")
                    handleEncryption(authenticatedCipher, dataToEncrypt)
                } else {
                    Timber.v("Authentication Cipher Failed!!!")
                    dismiss()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.v("Authentication Failed!!!")
                if (onFailedAuthenticate()) {
                    dismiss()
                }
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                Timber.v("Authentication error: $errString ($errorCode)")
                if (errorCode == 10 || errorCode == 13) {
                    biometricCallback.onAuthenticationCancelled()
                } else if (errorCode == 11) {
                    biometricCallback.onBiometricFingerprintNotEnrolled(false)
                } else {
                    biometricCallback.onAuthenticationError(errorCode, errString)
                }
            }
        }

        val biometricPrompt = BiometricPrompt(containerFragment, executor, callback)
        biometricPrompt.authenticate(prompt, BiometricPrompt.CryptoObject(cipher))
    }

    private fun promptBiometricDecryption(cipher: Cipher, prompt: BiometricPrompt.PromptInfo) {
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val authenticatedCipher = result.cryptoObject?.cipher
                if (authenticatedCipher != null) {
                    Timber.v("Authentication Success!!!")
                    handleDecryption(authenticatedCipher)
                } else {
                    Timber.v("Authentication Cipher Failed!!!")
                    onAuthenticationFailed()
                }
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                Timber.v("Authentication error: $errString ($errorCode)")
                if (errorCode == 10 || errorCode == 13) { //Canceled by user
                    biometricCallback.onAuthenticationCancelled()
                } else if (errorCode == 11) {
                    biometricCallback.onBiometricFingerprintNotEnrolled(true)
                } else {
                    biometricCallback.onAuthenticationError(errorCode, errString)
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.v("Authentication Failed")
                if (onFailedAuthenticate()) {
                    dismiss()
                }
            }
        }

        val biometricPrompt = BiometricPrompt(containerFragment, executor, callback)
        biometricPrompt.authenticate(prompt, BiometricPrompt.CryptoObject(cipher))
    }

    private fun handleEncryption(authenticatedCipher: Cipher, dataToEncrypt: String) {
        encrypt(authenticatedCipher, dataToEncrypt, object : CipherPromptCallback {
            override fun onSuccess(key: ByteArray, iv: ByteArray) {
                Timber.v("Encrypting: $key")
                saveEncrypted(key, iv)
                biometricCallback.onAuthenticationSucceeded()
            }

            override fun onError(error: String?) {
                Timber.v("Failed Encrypting w/ Cipher: $error")
                biometricCallback.onBiometricAuthenticationInternalError(error)
            }
        })
    }

    private fun handleDecryption(resultCipher: Cipher) {
        decrypt(resultCipher, object : CipherPromptCallback {
            override fun onSuccess(key: ByteArray, iv: ByteArray) {
                Timber.v("Decrypting: $key")
                val decrypted = String(key, Charsets.UTF_8)
                Timber.v("Decrypted Data: $decrypted")
                biometricCallback.onAuthenticationSucceeded(decrypted)
            }

            override fun onError(error: String?) {
                Timber.v("Failed Decrypting w/ Cipher: $error")
                biometricCallback.onBiometricAuthenticationInternalError(error)
            }
        })
    }

    private fun onFailedAuthenticate(): Boolean {
        Timber.v("Failed to authenticate, count: $failedCounter")
        return if (failedCounter == Int.MAX_VALUE) {
            failedCounter = 0
            true
        } else {
            failedCounter++
            false
        }
    }

    private fun biometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title!!)
            .setSubtitle(subtitle!!)
            .setDescription(description!!)
            .setConfirmationRequired(true)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText(negativeButtonText!!)
            .build()
    }

    private val isCancelled: Boolean
        get() = cancellationSignal.isCanceled

    private fun resetCancelSignal() {
        cancellationSignal = CancellationSignal()
    }

    class BiometricBuilder constructor(
        val containerFragment: Fragment,
        private val preferences: PreferencesRepository
    ) {
        var title: String? = null
        var subtitle: String? = null
        var description: String? = null
        var negativeButtonText: String? = null
        fun setTitle(title: String): BiometricBuilder {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: String): BiometricBuilder {
            this.subtitle = subtitle
            return this
        }

        fun setDescription(description: String): BiometricBuilder {
            this.description = description
            return this
        }

        fun setNegativeButtonText(negativeButtonText: String): BiometricBuilder {
            this.negativeButtonText = negativeButtonText
            return this
        }

        fun build(): BiometricPromptManager {
            return BiometricPromptManager(this, preferences)
        }
    }

    init {
        title = biometricBuilder.title
        subtitle = biometricBuilder.subtitle
        description = biometricBuilder.description
        negativeButtonText = biometricBuilder.negativeButtonText
    }
}