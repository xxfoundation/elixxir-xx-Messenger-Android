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
        get() = readBytes(R.raw.ud_elixxir_io)
    override val udContact: ByteArray
        get() = readBytes(R.raw.ud_contact_test)
    override val ndfCert: String
        get() = readText(R.raw.xx_ndf)

    private fun readBytes(resourceId: Int): ByteArray {
        return appContext().resources
            .openRawResource(resourceId)
            .use { it.readBytes() }
    }

    private fun readText(resourceId: Int): String {
        return appContext().resources
            .openRawResource(resourceId)
            .bufferedReader()
            .use { it.readText() }
    }
}