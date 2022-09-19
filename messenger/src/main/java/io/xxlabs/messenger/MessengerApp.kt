package io.xxlabs.messenger

import android.app.Application
import io.elixxir.xxclient.utils.LogWriter
import io.xxlabs.messenger.config.ClientBridge
import io.xxlabs.messenger.config.DefaultClient
import timber.log.Timber

class MessengerApp : Application() {

    val logger: LogWriter by lazy {
        { Timber.d(it) }
    }

    val clientBridge: ClientBridge by lazy {
        DefaultClient()
    }
}