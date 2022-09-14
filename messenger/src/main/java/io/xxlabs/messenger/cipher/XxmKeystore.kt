package io.xxlabs.messenger.cipher

import android.security.keystore.KeyProperties
import io.xxlabs.messenger.util.fromBase64toByteArray
import io.xxlabs.messenger.util.toBase64String
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
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

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    override fun rsaDecryptPwd(): ByteArray {
        val encryptedBytes = preferences.userSecret.fromBase64toByteArray()
        val key = getPrivateKey()
        val cipher1 = Cipher.getInstance(KEYSTORE_ALGORITHM)
        println("[BASE KEYSTORE] Initializing Decrypt")
        cipher1.init(Cipher.DECRYPT_MODE, key, cipherMode)
        val decryptedBytes = cipher1.doFinal(encryptedBytes)
        println("[BASE KEYSTORE] Decrypted: ${decryptedBytes.toBase64String()}")
        return decryptedBytes
    }

    private fun getPrivateKey(): PrivateKey? {
        val keyStore: KeyStore = getKeystore()
        return keyStore.getKey(KEY_ALIAS, null) as PrivateKey?
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