package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerLoad(private val env: MessengerEnvironment) {

    operator fun invoke() {
        env.cMix = env.loadCMix(
            storageDir = env.storageDir,
            password = env.passwordStorage.load(),
            cMixParams = env.getCMixParams().getOrThrow()
        ).getOrThrow()
    }
}