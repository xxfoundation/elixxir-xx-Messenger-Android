package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerCreate(private val env: MessengerEnvironment) {

    operator fun invoke() {
        val ndfData = env
            .downloadNDF(env.ndfEnvironment)
            .getOrThrow()
            .decodeToString()
        val password = env.generateSecret()
        env.passwordStorage.save(password)

        val storageDir = env.storageDir
        env.fileManager.removeItem(storageDir)
        env.fileManager.createDirectory(storageDir)

        env.newCMix(
            ndfJson = ndfData,
            storageDir = storageDir,
            password = password,
            registrationCode = ""
        )
    }
}