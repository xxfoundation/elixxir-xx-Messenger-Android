package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.UdMultiLookupResultListener
import io.elixxir.xxclient.utils.UserId
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerLookupContacts(private val env: MessengerEnvironment) {

    operator fun invoke(
        idsList: List<UserId>,
        listener: UdMultiLookupResultListener
    ) {
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
        val ud = env.ud ?: throw MessengerException.NotLoaded("UD")

        env.multiLookupUD(
            e2eId = e2e.id,
            contact = ud.contact,
            lookupIds = idsList,
            listener = listener,
            singleUeParams = env.getSingleUseParams().getOrThrow()
        )
    }
}