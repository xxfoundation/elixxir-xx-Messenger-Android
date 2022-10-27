package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.GroupMessageListener
import io.elixxir.xxclient.callbacks.GroupRequestListener
import io.elixxir.xxclient.groupchat.GroupChat
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerStartGroupChat(private val env: MessengerEnvironment) {

    operator fun invoke(
        requestListener: GroupRequestListener,
        messageListener: GroupMessageListener
    ): GroupChat {
        val e2eId = env.e2e?.id ?: throw MessengerException.NotLoaded("E2E")
        env.groupListeners.apply {
            setMessageListener(messageListener)
            setRequestListener(requestListener)
        }
        return env.bindings.newGroupChat(
            e2eId,
            env.groupListeners.getRequestListener(),
            env.groupListeners.getMessageListener()
        )
    }
}