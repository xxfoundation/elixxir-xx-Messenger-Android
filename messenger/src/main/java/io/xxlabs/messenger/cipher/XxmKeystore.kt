package io.xxlabs.messenger.cipher

import android.security.KeyChain.getPrivateKey
import android.security.keystore.KeyProperties
import bindings.Bindings
import io.xxlabs.messenger.util.fromBase64toByteArray
import io.xxlabs.messenger.util.toBase64String
import timber.log.Timber
import java.security.*
import java.security.spec.MGF1ParameterSpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

class XxmKeystore : KeyStoreManager {

    private val preferences: CipherPreferences by lazy {
        CipherPrefs()
    }

    override suspend fun generatePassword(): Result<Unit> {
        val bytesNumber: Long = 64
        var initialT = System.currentTimeMillis()
        Timber.v("[BASE KEYSTORE] Generating a password...")
        Timber.d("[BASE KEYSTORE] Generating a password with $bytesNumber bytes")

        var secret: ByteArray
        do {
            secret = Bindings.generateSecret(bytesNumber)
            Timber.d("[BASE KEYSTORE] Password (Bytearray): $secret")
            Timber.d("[BASE KEYSTORE] Password (String64): ${secret.toBase64String()}")
            Timber.v("[BASE KEYSTORE] total generation time: ${System.currentTimeMillis() - initialT}ms")

            val isAllZeroes = byteArrayOf(bytesNumber.toByte()).contentEquals(secret)
            Timber.d("[BASE KEYSTORE] IsAllZeroes: $isAllZeroes")
        } while (isAllZeroes)

        initialT = System.currentTimeMillis()
        rsaEncryptPwd(secret)
        Timber.v("[BASE KEYSTORE] total encryption time: ${System.currentTimeMillis() - initialT}ms")
        onFinished.invoke()
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    private fun rsaEncryptPwd(pwd: ByteArray): ByteArray {
        Timber.d("[BASE KEYSTORE] Bytecount: ${pwd.size}")
        Timber.d("[BASE KEYSTORE] Before encryption: ${pwd.toBase64String()}")
        val secretKey = getPublicKey()

        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, cipherMode)
        val encryptedBytes = cipher.doFinal(pwd)
        Timber.v("[BASE KEYSTORE] Encrypted: ${encryptedBytes.toBase64String()}")
        preferences.userSecret = encryptedBytes.toBase64String()
        return encryptedBytes
    }

        @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    override fun rsaDecryptPwd(): Result<ByteArray> {
        return try {
            val encryptedBytes = preferences.userSecret.fromBase64toByteArray()
            val key = getPrivateKey()
            val cipher1 = Cipher.getInstance(KEYSTORE_ALGORITHM)
            println("[BASE KEYSTORE] Initializing Decrypt")
            cipher1.init(Cipher.DECRYPT_MODE, key, cipherMode)
            val decryptedBytes = cipher1.doFinal(encryptedBytes)
            println("[BASE KEYSTORE] Decrypted: ${decryptedBytes.toBase64String()}")

            Result.success(decryptedBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
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