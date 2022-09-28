package io.elixxir.data.session.data

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.elixxir.core.common.Config
import io.elixxir.core.preferences.model.KeyStorePreferences
import io.elixxir.data.session.util.fromBase64toByteArray
import io.elixxir.data.session.util.toBase64String
import io.elixxir.xxclient.bindings.Bindings
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.security.*
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAKeyGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class XxmKeyStore @Inject internal constructor(
    private val bindings: Bindings,
    private val prefs: KeyStorePreferences,
    config: Config,
) : KeyStoreManager, Config by config {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    private val publicKey: PublicKey by lazy {
        keyStore.getCertificate(KEY_ALIAS).publicKey
    }

    private val privateKey: PrivateKey?
        get() = keyStore.getKey(KEY_ALIAS, null) as PrivateKey?

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

    override suspend fun generatePassword(): Result<Unit> = withContext(dispatcher) {
        try {
            generateNewKeys()
            val duration = measureTimeMillis {
                generateSecret()
            }
            log("Total encryption time: $duration ms")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateNewKeys() {
        deletePreviousKeys()
        keyPairGenerator.genKeyPair()
    }

    private fun deletePreviousKeys() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            log("Deleting key alias")
            keyStore.deleteEntry(KEY_ALIAS)
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

    private fun rsaEncryptPwd(pwd: ByteArray): ByteArray {
        log("Byte count: ${pwd.size}")
        log("Before encryption: ${pwd.toBase64String()}")

        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, cipherMode)
        val encryptedBytes = cipher.doFinal(pwd)
        log("Encrypted: ${encryptedBytes.toBase64String()}")
        prefs.userSecret = encryptedBytes.toBase64String()

        return encryptedBytes
    }

    override suspend fun decryptPassword(): Result<ByteArray> = withContext(dispatcher) {
        try {
            Result.success(decryptSecret())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decryptSecret(): ByteArray {
        if (prefs.userSecret.isNullOrBlank()) {
            throw IllegalStateException("Key has not been saved yet!")
        }

        val encryptedBytes = prefs.userSecret?.fromBase64toByteArray()
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