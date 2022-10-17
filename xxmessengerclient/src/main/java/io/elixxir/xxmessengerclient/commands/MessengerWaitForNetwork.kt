package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerWaitForNetwork(private val env: MessengerEnvironment) {

    operator fun invoke(timeoutMs: Long = 30_000) {
        val cMix = env.cMix ?: throw MessengerException.NotLoaded("CMix")

        try {
            cMix.waitForNetwork(timeoutMs)
        } catch (e: Exception) {
            throw MessengerException.TimedOut()
        }
    }
}