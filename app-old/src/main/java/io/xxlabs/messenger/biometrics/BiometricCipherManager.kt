package io.xxlabs.messenger.biometrics

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.util.Base64
import io.xxlabs.messenger.repository.PreferencesRepository
import timber.log.Timber
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.text.Charsets.UTF_8

abstract class BiometricCipherManager(val preferencesRepository: PreferencesRepository) {
    fun encryptPrompt(
        generationCallback: CipherGenerationCallback,
        cancelOnError: Boolean = false
    ) {
        val secretKey = getKey()
        try {
            Timber.v("Generating AES Cipher")
            val cipher = getEncryptCipher(secretKey)
            generationCallback.onCipherGenerated(cipher)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("Error: ${e.localizedMessage}")
            when (e) {
                is UserNotAuthenticatedException,
                is KeyPermanentlyInvalidatedException -> {
                    Timber.v("Key File was Invalidated, generating another cipher")
                    removeKey()
                    if (cancelOnError) {
                        generationCallback.onCipherGenerationError(e.localizedMessage)
                    } else {
                        encryptPrompt(generationCallback, true)
                    }
                }
                is KeyStoreException,
                is CertificateException,
                is UnrecoverableKeyException,
                is IOException,
                is NoSuchAlgorithmException,
                is InvalidKeyException -> generationCallback.onCipherGenerationError("Failed to init Cipher")
                else -> generationCallback.onCipherGenerationError(e.localizedMessage)
            }
        }
    }

    fun decryptPrompt(generationCallback: CipherGenerationCallback) {
        try {
            val secretKey = getKey()
            val encryptedData = getEncryptedData()
            val initializationVector = encryptedData.iv
            val cipher = getDecryptCipher(secretKey, initializationVector)
            generationCallback.onCipherGenerated(cipher)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("Error: ${e.localizedMessage}")
            generationCallback.onCipherGenerationError(e.localizedMessage)
        }
    }

    internal fun encrypt(
        cipher: Cipher,
        data: String,
        callback: CipherPromptCallback
    ) {
        try {
            Timber.d("INITIAL IV: ${Arrays.toString(cipher.iv)}")
            Timber.d("INITIAL DATA: $data")
            val dataInBytes = data.toByteArray()
            val encryptedCipher = cipher.doFinal(dataInBytes)
            val encryptedString = String(encryptedCipher, UTF_8)
            Timber.d("Encrypted text: $encryptedString")
            if (!encryptedString.isNullOrEmpty()) {
                callback.onSuccess(encryptedCipher, cipher.iv)
            } else {
                Timber.e("Failed to encrypt message!")
                callback.onError("Encryption is not valid")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("Failed to encrypt message! ${e.localizedMessage}")
            callback.onError(e.localizedMessage)
        }
    }

    internal fun decrypt(
        cipher: Cipher,
        callback: CipherPromptCallback
    ) {
        try {
            val encryptedData = getEncryptedData()
            val key = encryptedData.key
            val encodedDecryption = cipher.doFinal(key)
            val decryptedString = String(encodedDecryption, UTF_8)
            Timber.d("Encrypted text: $decryptedString")
            if (!decryptedString.isNullOrEmpty()) {
                callback.onSuccess(encodedDecryption, cipher.iv)
            } else {
                Timber.e("Failed to decrypt message!")
                callback.onError("Decryption is not valid")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("Failed to encrypt message! ${e.localizedMessage}")
            callback.onError("Failed to decrypt message! ${e.localizedMessage}")
        }
    }

    internal fun saveEncrypted(key: ByteArray, iv: ByteArray) {
        val base64IV = Base64.encodeToString(
            iv,
            Base64.DEFAULT
        )
        val cipheredKey = Base64.encodeToString(
            key,
            Base64.DEFAULT
        ) + "-" + base64IV

        Timber.d("INITIAL IV (B64): $base64IV")

        preferencesRepository.userBiometricKey = cipheredKey
    }

    private fun generateKey(): Boolean {
        return try {
            Timber.v("Creating key in KeyStore")

            keystore.load(null)
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keygenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                keygenSpec.setUserAuthenticationParameters(
                    10,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
            } else {
                keygenSpec.setUserAuthenticationValidityDurationSeconds(10)
            }

            keyGenerator.init(keygenSpec.build())
            keyGenerator.generateKey()
            Timber.v("KeyStore Key was successfully created!")
            true
        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
            false
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
            false
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
            false
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
            false
        } catch (exc: CertificateException) {
            exc.printStackTrace()
            false
        } catch (exc: IOException) {
            exc.printStackTrace()
            false
        }
    }

    private fun getEncryptedData(): EncryptedData {
        val encrypted = preferencesRepository.userBiometricKey
        Timber.d("ENCRYPTED B64: $encrypted")
        val split = encrypted.split("-").toTypedArray()
        val key = split[0]
        val iv = split[1]

        val decodedKey = Base64.decode(
            key.toByteArray(),
            Base64.DEFAULT
        )
        val decodedIv = Base64.decode(
            iv.toByteArray(),
            Base64.DEFAULT
        )

        Timber.d("FINAL IV: %s", Arrays.toString(decodedIv))
        Timber.d("ENCRYPTED KEY: $key")
        Timber.d("ENCRYPTED IV: $iv")

        return EncryptedData(decodedKey, decodedIv)
    }

    private fun getDecryptCipher(key: Key, iv: ByteArray): Cipher =
        Cipher.getInstance(keyTransformation())
            .apply { init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv)) }

    private fun getEncryptCipher(key: Key): Cipher =
        Cipher.getInstance(keyTransformation()).apply { init(Cipher.ENCRYPT_MODE, key) }

    private fun getKey(retry: Boolean = true): SecretKey {
        if (!keyExists()) {
            generateKey()
        }

        return try {
            keystore.getKey(
                KEY_ALIAS,
                null
            ) as SecretKey
        } catch (e: UnrecoverableKeyException) {
            Timber.e("Error: ${e.localizedMessage}")
            removeKey()
            if (retry) {
                return getKey(false)
            } else {
                throw e
            }
        }
    }

    private fun removeKey() {
        Timber.v("Removing existing key on KeyStore")
        if (keyExists()) {
            val keyStore =
                KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    private fun keyExists(): Boolean {
        keystore.load(null)
        return keystore.containsAlias(KEY_ALIAS)
    }

    companion object {
        val keystore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        private const val KEY_ALIAS = "xxmessengerfingerprintk"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private fun keyTransformation() =
            listOf(ALGORITHM, BLOCK_MODE, PADDING).joinToString(separator = "/")
    }
}