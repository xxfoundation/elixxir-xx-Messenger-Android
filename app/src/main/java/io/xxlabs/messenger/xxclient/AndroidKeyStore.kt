package io.xxlabs.messenger.xxclient

import android.util.Base64
import io.elixxir.xxmessengerclient.utils.PasswordStorage
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import javax.inject.Inject

class AndroidKeyStore @Inject constructor(
    private val prefs: PreferencesRepository
) : PasswordStorage {

    override fun save(password: ByteArray) {
        prefs.userSecret = password.toBase64String(Base64.DEFAULT)
    }

    override fun load(): ByteArray {
        return prefs.userSecret.fromBase64toByteArray(Base64.DEFAULT)
    }

    override fun clear() {
        prefs.userSecret = ""
    }
}