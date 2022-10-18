package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.NetworkFollowerStatus
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerDestroy(private val env: MessengerEnvironment) {

    operator fun invoke(
        runningProcessPollingIntervalMs: Long = 1000
    ) {
        val cmix = env.cMix ?: throw(MessengerException.NotLoaded("CMix"))
        if (cmix.getNetworkFollowerStatus() == NetworkFollowerStatus.Running) {
            cmix.stopNetworkFollower()
        }
        while (cmix.hasRunningProcesses()) {
            env.sleep(runningProcessPollingIntervalMs)
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