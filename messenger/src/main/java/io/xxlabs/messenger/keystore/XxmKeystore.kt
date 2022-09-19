package io.xxlabs.messenger.keystore

import android.security.keystore.KeyProperties
import io.elixxir.xxclient.bindings.Bindings
import io.xxlabs.messenger.util.fromBase64toByteArray
import io.xxlabs.messenger.util.toBase64String
import java.security.*
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import kotlin.system.measureTimeMillis

class XxmKeystore(
    private val bindings: Bindings,
    private val preferences: CipherPreferences,
    private val log: (String) -> Unit
) : KeyStoreManager {

    override suspend fun generatePassword(): Result<Unit> {
        deletePreviousKeys()

        return try {
            val duration = measureTimeMillis {
                generateSecret()
            }
            log("Total encryption time: $duration ms")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateSecret(length: Long = DEFAULT_SECRET_LENGTH) {
        log("Generating a password with $length bytes")

        var secret: ByteArray
        do {
            secret = bindings.generateSecret(length)
            log("Password (Bytearray): $secret")
            log("Password (String64): ${secret.toBase64String()}")

            val isAllZeroes = byteArrayOf(length.toByte()).contentEquals(secret)
            log("IsAllZeroes: $isAllZeroes")
        } while (isAllZeroes)

        rsaEncryptPwd(secret)
    }

    private fun deletePreviousKeys() {
        val keystore = getKeystore()
        if (keystore.containsAlias(KEY_ALIAS)) {
            log("Deleting key alias")
            keystore.deleteEntry(KEY_ALIAS)
        }
    }

    private fun rsaEncryptPwd(pwd: ByteArray): ByteArray {
        log("Byte count: ${pwd.size}")
        log("Before encryption: ${pwd.toBase64String()}")
        val secretKey = getPublicKey()

        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, cipherMode)
        val encryptedBytes = cipher.doFinal(pwd)
        log("Encrypted: ${encryptedBytes.toBase64String()}")
        preferences.userSecret = encryptedBytes.toBase64String()

        return encryptedBytes
    }

    override fun rsaDecryptPwd(): Result<ByteArray> {
        return try {
            Result.success(decryptSecret())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decryptSecret(): ByteArray {
        val encryptedBytes = preferences.userSecret.fromBase64toByteArray()
        val key = getPrivateKey()
        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        println("Initializing Decrypt")
        cipher.init(Cipher.DECRYPT_MODE, key, cipherMode)

        return cipher.doFinal(encryptedBytes)
    }

    private fun getPrivateKey(): PrivateKey? {
        val keyStore: KeyStore = getKeystore()
        return keyStore.getKey(KEY_ALIAS, null) as PrivateKey?
    }

    private fun getPublicKey(): PublicKey {
        return getKeystore().getCertificate(KEY_ALIAS).publicKey
    }

    private fun getKeystore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }

    companion object {
        private const val DEFAULT_SECRET_LENGTH = 64L
        private const val KEY_ALIAS = "xxmessengerpvk"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_PURPOSE =
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        private const val KEYSTORE_ALGORITHM = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"
        private const val KEY_SIZE = 2048
        private val cipherMode = OAEPParameterSpec(
            "SHA-1", "MGF1",
            MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT
        );
    }
}