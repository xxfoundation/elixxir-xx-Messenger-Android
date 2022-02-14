package io.xxlabs.messenger.ui.base

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import bindings.Bindings
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import timber.log.Timber
import java.security.*
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAKeyGenParameterSpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject

open class BaseKeystoreActivity : BaseInjectorActivity() {
    @Inject
    lateinit var preferences: PreferencesRepository

    fun generatePassword(onFinished: () -> (Unit)) {
        val bytesNumber: Long = 64
        var initialT = System.currentTimeMillis()
        Timber.v("[BASE KEYSTORE] Generating a password...")
        Timber.d("[BASE KEYSTORE] Generating a password with $bytesNumber bytes")

        var secret: ByteArray
        do {
            secret = Bindings.generateSecret(bytesNumber)
            Timber.d("[BASE KEYSTORE] Password (Bytearray): $secret")
            Timber.d("[BASE KEYSTORE] Password (String64): ${secret.toBase64String(Base64.DEFAULT)}")
            Timber.v("[BASE KEYSTORE] total generation time: ${System.currentTimeMillis() - initialT}ms")

            val isAllZeroes = byteArrayOf(bytesNumber.toByte()).contentEquals(secret)
            Timber.d("[BASE KEYSTORE] IsAllZeroes: $isAllZeroes")
        } while (isAllZeroes)

        initialT = System.currentTimeMillis()
        rsaEncryptPwd(secret)
        Timber.v("[BASE KEYSTORE] total encryption time: ${System.currentTimeMillis() - initialT}ms")
        onFinished.invoke()
    }

    fun deletePreviousKeys() {
        val keystore = getKeystore()
        if (keystore.containsAlias(KEY_ALIAS)) {
            Timber.v("[BASE KEYSTORE] Deleting key alias")
            keystore.deleteEntry(KEY_ALIAS)
        }
    }

    fun checkGenerateKeys(): Boolean {
        return try {
            val areKeysGenerated = generateKeys()
            if (areKeysGenerated) {
                Timber.v("[BASE KEYSTORE] Keystore keys successfully generated")
                true
            } else {
                Timber.e("[BASE KEYSTORE] Error generating keystore keys")
                false
            }
        } catch (err: Exception) {
            Timber.e("[BASE KEYSTORE] Error generating the keys...")
            Timber.d(err.localizedMessage)
            false
        }
    }

    private fun generateKeys(): Boolean {
        return try {
            val keystore = getKeystore()
            if (!keystore.containsAlias(KEY_ALIAS)) {
                Timber.d("[BASE KEYSTORE] Keystore alias does not exist, credentials")
                val keyGenerator = getKeyPairGenerator()
                keyGenerator.genKeyPair()
            } else {
                Timber.d("[BASE KEYSTORE] Keystore alias already exist")
            }
            true
        } catch (err: Exception) {
            err.printStackTrace()
            false
        }
    }

    private fun getKeyPairGenerator(): KeyPairGenerator {
        val keyGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )
        val keyGenParameterSpec = getKeygenParamSpec()
        keyGenerator.initialize(keyGenParameterSpec)
        return keyGenerator
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

    private fun getKeygenParamSpec(): KeyGenParameterSpec {
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KEY_PURPOSE
        ).setAlgorithmParameterSpec(RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4))
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setDigests(KeyProperties.DIGEST_SHA1)
            .setRandomizedEncryptionRequired(true)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            keyGenSpec.setUserAuthenticationParameters(
                1000,
                KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
            )
        } else {
            keyGenSpec.setUserAuthenticationValidityDurationSeconds(1000)
        }

        return keyGenSpec.build()
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
        Timber.d("[BASE KEYSTORE] Before encryption: ${pwd.toBase64String(Base64.DEFAULT)}")
        val secretKey = getPublicKey()

        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, cipherMode)
        val encryptedBytes = cipher.doFinal(pwd)
        Timber.v("[BASE KEYSTORE] Encrypted: ${encryptedBytes.toBase64String(Base64.DEFAULT)}")
        preferences.userSecret = encryptedBytes.toBase64String(Base64.DEFAULT)
        return encryptedBytes
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun rsaDecryptPwd(): ByteArray {
        val encryptedBytes = preferences.userSecret.fromBase64toByteArray(Base64.DEFAULT)
        val key = getPrivateKey()
        val cipher1 = Cipher.getInstance(KEYSTORE_ALGORITHM)
        println("[BASE KEYSTORE] Initializing Decrypt")
        cipher1.init(Cipher.DECRYPT_MODE, key, cipherMode)
        val decryptedBytes = cipher1.doFinal(encryptedBytes)
        println("[BASE KEYSTORE] Decrypted: ${decryptedBytes.toBase64String(Base64.DEFAULT)}")
        return decryptedBytes
    }

    open fun isHardwareBackedKeyStore(): Boolean {
        return try {
            val privateKey = getPrivateKey()
            val keyFactory = KeyFactory.getInstance(privateKey?.algorithm, ANDROID_KEYSTORE)
            val keyInfo: KeyInfo = keyFactory.getKeySpec(privateKey, KeyInfo::class.java)
            val securityLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT ||
                    keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX)
            } else {
                keyInfo.isInsideSecureHardware
            }

            return securityLevel
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
            e.printStackTrace()
            false
        }
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