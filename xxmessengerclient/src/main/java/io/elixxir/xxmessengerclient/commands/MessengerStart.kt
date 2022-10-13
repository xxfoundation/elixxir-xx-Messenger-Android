package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.NetworkFollowerStatus
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerStart(private val env: MessengerEnvironment) {

    operator fun invoke(timeoutMs: Long = 30_000) {
        env.cMix?.let {
            if (it.getNetworkFollowerStatus() != NetworkFollowerStatus.Running) {
                it.startNetworkFollower(timeoutMs)
            }
        } ?: throw MessengerException.NotLoaded()
    }
}