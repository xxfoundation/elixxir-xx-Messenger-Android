package io.xxlabs.messenger.keystore

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.elixxir.xxclient.bindings.Bindings
import io.xxlabs.messenger.util.fromBase64toByteArray
import io.xxlabs.messenger.util.toBase64String
import java.security.*
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAKeyGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import kotlin.system.measureTimeMillis

class XxmKeystore(
    private val bindings: Bindings,
    private val preferences: CipherPreferences,
    private val log: (String) -> Unit
) : KeyStoreManager {

    private val keystore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    private val publicKey: PublicKey by lazy {
        keystore.getCertificate(KEY_ALIAS).publicKey
    }

    private val privateKey: PrivateKey?
        get() = keystore.getKey(KEY_ALIAS, null) as PrivateKey?

    private val keyPairGenerator: KeyPairGenerator by lazy {
        KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        ).apply {
            initialize(keyGenParamSpec)
        }
    }

    private val keyGenParamSpec: KeyGenParameterSpec by lazy {
        KeyGenParameterSpec.Builder(KEY_ALIAS, KEY_PURPOSE)
            .setAlgorithmParameterSpec(RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4))
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setDigests(KeyProperties.DIGEST_SHA1)
            .setRandomizedEncryptionRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(
                        1000,
                        KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                    )
                } else {
                    setUserAuthenticationValidityDurationSeconds(1000)
                }
            }.build()
    }

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
        if (keystore.containsAlias(KEY_ALIAS)) {
            log("Deleting key alias")
            keystore.deleteEntry(KEY_ALIAS)
        }
    }

    private fun rsaEncryptPwd(pwd: ByteArray): ByteArray {
        log("Byte count: ${pwd.size}")
        log("Before encryption: ${pwd.toBase64String()}")

        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, cipherMode)
        val encryptedBytes = cipher.doFinal(pwd)
        log("Encrypted: ${encryptedBytes.toBase64String()}")
        preferences.userSecret = encryptedBytes.toBase64String()

        return encryptedBytes
    }

    override suspend fun generateKeys(): Result<Unit> {
        return try {
            generateIfMissing()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateIfMissing() {
        if (!keystore.containsAlias(KEY_ALIAS)) {
            log("Keystore alias does not exist, creating new one.")
            keyPairGenerator.genKeyPair()
        } else {
            log("Keystore alias already exists")
        }
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
        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        log("Initializing Decrypt")
        cipher.init(Cipher.DECRYPT_MODE, privateKey, cipherMode)

        return cipher.doFinal(encryptedBytes)
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
            "SHA-1",
            "MGF1",
            MGF1ParameterSpec.SHA1,
            PSource.PSpecified.DEFAULT
        )
    }
}