package io.xxlabs.messenger.xxclient

import android.content.Context
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.PasswordStorage
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.appContext
import javax.inject.Inject

class DevEnvironment @Inject constructor(
    context: Context,
    override val passwordStorage: PasswordStorage,
): MessengerEnvironment() {
    override val storageDir: String = context.filesDir.path
    override val udCert: ByteArray
        get() = rawBytes(R.raw.ud_elixxir_io)
    override val udContact: ByteArray
        get() = rawBytes(R.raw.ud_contact_test)

    private fun rawBytes(resourceId: Int): ByteArray {
        return appContext().resources
            .openRawResource(resourceId)
            .use { it.readBytes() }
    }
}