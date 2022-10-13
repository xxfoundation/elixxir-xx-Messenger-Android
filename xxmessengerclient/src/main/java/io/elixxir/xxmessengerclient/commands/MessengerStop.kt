package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.NetworkFollowerStatus
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerStop(private val env: MessengerEnvironment) {

    operator fun invoke(
        retryIntervalMs: Long = 1000,
        retries: Int = 10
    ) {
        var _retries = retries
        val cMix = env.cMix ?: throw MessengerException.NotLoaded()

        if (cMix.getNetworkFollowerStatus() != NetworkFollowerStatus.Running)
            return

        cMix.stopNetworkFollower()
        env.sleep(retryIntervalMs)

        while (_retries > 0 && cMix.hasRunningProcesses()) {
            env.sleep(retryIntervalMs)
            _retries -= 1
        }

        if (cMix.hasRunningProcesses()) throw MessengerException.TimedOut()
    }
}