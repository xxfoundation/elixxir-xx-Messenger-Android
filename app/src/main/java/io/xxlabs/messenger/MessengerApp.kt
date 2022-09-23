package io.xxlabs.messenger

import android.app.Application
import io.xxlabs.messenger.config.ClientBridge
import io.xxlabs.messenger.config.DefaultClient

class MessengerApp : Application() {

    val clientBridge: ClientBridge by lazy {
        DefaultClient()
    }
}