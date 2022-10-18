package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.NetworkFollowerStatus
import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerDestroy(private val env: MessengerEnvironment) {

    operator fun invoke(
        runningProcessPollingIntervalMs: Long = 1000
    ) {
        env.cMix?.apply {
            if (getNetworkFollowerStatus() == NetworkFollowerStatus.Running) {
                stopNetworkFollower()
            }
            while (hasRunningProcesses()) {
                env.sleep(runningProcessPollingIntervalMs)
            }
        }

        env.apply {
            ud = null
            e2e = null
            cMix = null
            isListeningForMessages = false
            fileManager.removeItem(env.storageDir)
            passwordStorage.clear()
        }
    }
}