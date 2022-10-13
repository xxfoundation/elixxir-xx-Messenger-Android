package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerIsCreated(private val env: MessengerEnvironment) {

    operator fun invoke(): Boolean {
        return !env.fileManager.isDirectoryEmpty(env.storageDir)
    }
}