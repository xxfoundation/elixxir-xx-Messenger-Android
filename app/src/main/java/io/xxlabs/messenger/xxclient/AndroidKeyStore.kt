package io.xxlabs.messenger.xxclient

import android.security.keystore.KeyProperties
import android.util.Base64
import io.elixxir.xxclient.password.PasswordStorage
import io.elixxir.xxclient.utils.Password
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BasePreferences
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.inject.Inject

class AndroidKeyStore @Inject constructor(
    private val prefs: PreferencesRepository
) : PasswordStorage {
    private val keystore by lazy {
         KeyStore.getInstance(ANDROID_KEYSTORE).apply {
             load(null)
         }
    }
    private val publicKey: PublicKey by lazy {
        keystore.getCertificate(KEY_ALIAS).publicKey
    }
    private val privateKey: PrivateKey?
        get() = keystore.getKey(KEY_ALIAS, null) as PrivateKey?

    override fun save(password: Password) {
        rsaEncryptPwd(password)
    }

    private fun rsaEncryptPwd(pwd: ByteArray) {
        val encryptedBytes = Cipher.getInstance(KEYSTORE_ALGORITHM).run {
            init(Cipher.ENCRYPT_MODE, publicKey, cipherMode)
            doFinal(pwd)
        }
        prefs.userSecret = encryptedBytes.toBase64String(Base64.DEFAULT)
    }

    override fun load(): Password {
        return rsaDecryptPwd()
    }

    private fun rsaDecryptPwd(): ByteArray {
        val encryptedBytes = prefs.userSecret.fromBase64toByteArray(Base64.DEFAULT)
        return Cipher.getInstance(KEYSTORE_ALGORITHM).run {
            init(Cipher.DECRYPT_MODE, privateKey, cipherMode)
            doFinal(encryptedBytes)
        }
    }

    override fun clear() {
        if (keystore.containsAlias(KEY_ALIAS)) {
            keystore.deleteEntry(KEY_ALIAS)
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
        )
    }
}