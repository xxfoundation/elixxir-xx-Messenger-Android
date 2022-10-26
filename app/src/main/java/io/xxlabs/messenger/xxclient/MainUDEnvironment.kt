package io.xxlabs.messenger.xxclient

import android.content.Context
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.PasswordStorage
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.appContext
import javax.inject.Inject

class MainUDEnvironment @Inject constructor(
    context: Context,
    override val passwordStorage: PasswordStorage,
): MessengerEnvironment() {
    override val storageDir: String = context.filesDir.path

    override val ndfCert: String
        get() = readText(R.raw.mainnet)

    private fun readText(resourceId: Int): String {
        return appContext().resources
            .openRawResource(resourceId)
            .bufferedReader()
            .use { it.readText() }
    }
}