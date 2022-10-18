package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.UdLookupResultListener
import io.elixxir.xxclient.utils.UserId
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerLookupContact(private val env: MessengerEnvironment) {

    operator fun invoke(
        query: UserId,
        listener: UdLookupResultListener
    ) {
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
        val ud = env.ud ?: throw MessengerException.NotLoaded("UD")

        env.lookupUD(
            e2eId = e2e.id,
            contact = ud.contact,
            listener = listener,
            lookupId = query,
            singleUseParams = env.getSingleUseParams().getOrThrow()
        )
    }
}