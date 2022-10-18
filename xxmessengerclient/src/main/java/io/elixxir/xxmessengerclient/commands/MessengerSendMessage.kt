package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.MessageType
import io.elixxir.xxclient.utils.Payload
import io.elixxir.xxclient.utils.UserId
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerSendMessage(private val env: MessengerEnvironment) {

    operator fun invoke(
        receiver: UserId,
        payload: Payload,
    ) {
        TODO("Use legacy implementation to call the E2E method directly")
//        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
//        val ud = env.ud ?: throw MessengerException.NotLoaded("UD")
//
//        val report = e2e.sendE2e(
//            messageType = MessageType.XxMessage,
//            receiverId = receiver,
//            payload = payload,
//            params = env.getE2EParams()
//        )
    }
}